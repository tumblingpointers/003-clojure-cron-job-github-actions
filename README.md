# Getting hands dirty with Clojure, GitHub Actions and IFTTT Webhooks for Personal Automation

As a software engineer, I cannot help but think of making my life more efficient. These are a few recent ideas I have had:
  - As my salary arrives, transfer it to different accounts based on a pre-set allocation
  - Turn the heater on or off depending on the room temperature
  - Update workout plans with new weights according to a progression plan
  - Send me tweets with more than 100 likes from last week from people I follow

When it comes to actually implementing these ideas, the choice is to either use existing automation tools like [IFTTT](https://ifttt.com/) or write my own scripts. Tools like IFTTT are great because they are easy to use and well-integrated with thousands of [services](https://ifttt.com/explore/services) but they lack the flexibility you can achieve with your own code - maybe you want to process data in a certain way or talk to an unsupported API?

On the other hand, writing code is not just about writing code but also about deploying, running and maintaining it. Plus, now you have to write code for every service yourself (even the ones supported by IFTTT).

Is there a way where we can get the best of both worlds?

1. Make use of [existing integrations](https://ifttt.com/explore/services)
1. Write custom code when necessary
1. Painless code maintenance/management
1. Free deployment

This article covers how we can achieve exactly that.

## Email me transcripts of new TED Talks

For my usecase, I will be implementing this idea I had about turning new [TED](https://www.ted.com/) videos into text so I can read them instead of taking out the time to watch them. This is the plan:

1. Watch out for new talks on their [YouTube Channel](https://www.youtube.com/@TED)
1. Find the talk on the [official website](https://www.ted.com/talks) and grab the transcript
1. Email it to myself

*Note: We could also grab the transcript from the Youtube video itself but that requires setting up a [Google Developer](https://developers.google.com/youtube/v3) account. Talking to the TED website is much easier.*

[IFTTT](https://ifttt.com/) has integrations which make #1 and #3 a piece of cake but nothing to help us with #2. This is a problem no-code approaches run into often - so how do we fill this gap?

## Just Code It

I know right, just code whatever is missing? Exactly but then we run into two problems:

### 1. How do we plug it into IFTTT?

Well, all we need is some way of triggering our code when IFTTT is done and some way of letting IFTTT know when our code is done. That is exactly what their [webhooks](https://ifttt.com/maker_webhooks) help with. Similar functionality exists in [Zapier](https://zapier.com/page/webhooks/) and [Apple Shortcuts](https://support.apple.com/en-hk/guide/shortcuts/apd58d46713f/ios) so we can mix our code with other tools too.

### 2. Where do we run it?

Writing code is just a piece of the puzzle. Maintaining, deploying and running it is where it gets trickier. Here are a few options:

- Run it on your PC
- Buy a [virtual machine](https://www.digitalocean.com/products/droplets) and run it there
- Run it [serverless](https://aws.amazon.com/lambda/) somewhere

In addition to costing money, the above options require even more code depending on whether you decide to build any of the following:

- a server to receive triggers
- authentication/authorization
- continuous deployment

All this to say that there is a solution: [GitHub Actions](https://github.com/features/actions)

If you haven't heard of it or [similar solutions](https://docs.gitlab.com/ee/ci/pipelines/) before, these are workflows that run in your `git` repository and can be [triggered](https://docs.github.com/en/actions/using-workflows/events-that-trigger-workflows) in all sorts of ways. Common usecases involve testing, building and deploying code but the possibilities are endless.

All we need to do is write a `workflow` which starts when `https://api.github.com/repos/tumblingpointers/003-clojure-cron-job-github-actions/actions/workflows/{workflow-id}/dispatches` is hit. Inside the workflow, we can run our custom code and hit IFTTT when we are done.

For personal use, the [usage limits](https://docs.github.com/en/billing/managing-billing-for-github-actions/about-billing-for-github-actions#included-storage-and-minutes) are more than enough so we essentially have an easy way of running our code for free. 🥳

## Back to TED

Let's get our hands dirty and actually code it out. We will start with the custom code as we need a workflow url before we can ask IFTTT to hit it.

We can use any language we want for this part - personally I prefer Clojure for scripts as it's concise and a delight to use.

I won't go into a lot of details but these are the key parts:

### 1. GitHub Actions Workflow to receive the trigger from IFTTT

I create a `.github/workflows/ted-talk-transcript.yaml` workflow which can be triggered manually from the UI or by hitting `https://api.github.com/repos/tumblingpointers/003-clojure-cron-job-github-actions/actions/workflows/ted-talk-transcript.yaml/dispatches`. It needs one input: `talk_title`.

```yaml
name: Ted Talk Transcript 🏃

on:
  workflow_dispatch:
    inputs:
      talk_title:
        description: Title of the TED Talk
        required: true

jobs:
  ted-talk-transcript:
    steps:
      - name: Run
        env:
          IFTT_API_KEY: ${{ secrets.IFTT_API_KEY }}
        run: clojure -X core/run :job ted-talk-transcript :input '"${{ github.event.inputs.talk_title }}"'
```

### 2. Find the talk on TED.com

Once I have the title of the talk from Youtube, I search for it on `https://www.ted.com/talks` and look through the html to find all links of the pattern `/talks/video-id` using [regex](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions). At the end, I do a `distinct` because each talk has two links in the search results. 

```clj
(def base-url "https://www.ted.com")

(defn- search-talks [query]
  (let [query-url   (str base-url "/talks?sort=relevance&q=" query)
        results     (-> (hc/get query-url)
                        :body)
        links       (re-seq #"/talks/(.*)'" results)
        video-ids   (distinct (map second links))]
    video-ids))
```

### 3. Grab the transcript

Using [Inspect network activity](https://developer.chrome.com/docs/devtools/network/) on their [talk videos](https://www.ted.com/talks/dan_finkel_can_you_steal_the_most_powerful_wand_in_the_wizarding_world) I find out that they have a neat [GraphQL](https://graphql.org/) endpoint: <https://www.ted.com/graphql>. All I have to do is to provide the video id to get the transcript back.

```clj
...
  (let [gql (str "{translation(language:\"en\", videoId:\""
                 video-id
                 "\") {id paragraphs { cues { text }}}}")
        response (hc/post (str base-url "/graphql") {:form-params {:operationName nil
                                                                   :query gql}
                                                     :content-type :json})
        json (-> response
                 :body
                 (cheshire/parse-string true))
...
```

### 4. Send the transcript to IFTTT

Once I have the transcript, I send it to IFTTT which emails it to me.

```clj
(let [url (str "https://maker.ifttt.com/trigger/email/json/with/key/" (System/getenv "IFTT_API_KEY"))]
  (hc/post url {:form-params payload
                :content-type :json}))
```