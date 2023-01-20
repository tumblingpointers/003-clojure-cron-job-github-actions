# Getting hands dirty with Clojure and GitHub Actions for Personal Automation

As a software engineer, I cannot help but think of making my life more efficient. These are a few recent ideas I have had:
  - As my salary arrives, transfer it to different accounts based on a pre-set allocation
  - Turn the heater on or off depending on the room temperature
  - Update workout plans with new weights according to a progression plan
  - Send me tweets with more than 100 likes from last week from people I follow

As writing code is not just about writing code but also about deploying, running and maintaining it I have avoided it so far. Usually I try to make do with existing tools like Apple Shortcuts, Apple Home or IFTT.
However, recently I started thinking of ways to make self-written automation easier. Let's summarize the objectives:

1. Concise and easy-to-maintain code
1. Zero-cost
1. No self-maintained servers

I am pretty sure there are many solutions to this but this is what I came up with:

1. Maintain a public repository for the code -> Motivates me to write readable code as people might see it
1. Write code in Clojure -> Clojure is concise and I like to use it
1. GitHub Actions to run code -> Free for Public Repositories and I can schedule daily/weekly runs
1. Keep all automation code in one repository -> Easy to maintain

Let's get started.

## 1. Setup a Clojure Project

### 1.1 Installing Clojure

First things first, let's install `clojure` locally. For [Windows Subsytem for Linux](https://learn.microsoft.com/en-us/windows/wsl/install), this is what I ran. You can find instructions for your specific system at [Install Clojure](https://clojure.org/guides/install_clojure).

```sh
sudo apt-get update
sudo apt install openjdk-17-jre-headless
sudo apt install rlwrap
curl -O https://download.clojure.org/install/linux-install-1.11.1.1208.sh && chmod +x linux-install-1.11.1.1208.sh
sudo ./linux-install-1.11.1.1208.sh
rm ./linux-install-1.11.1.1208.sh
```

### 1.2 Basic Structure

1. Let's start with [.gitignore](./.gitignore) so we don't commit files we don't need to. (You can find it)
2. To manage our project, we will use the default [Clojure CLI Tools](https://clojure.org/guides/deps_and_cli). To do that we need to create a basic `deps.edn` file in the root:

```clj
{:deps {org.clojure/clojure {:mvn/version "1.11.1"}}
 :aliases {:dev {:extra-paths ["test"]}
           ;; clj -X:test-runner
           :test-runner {:extra-paths ["test"]
                         :extra-deps {io.github.cognitect-labs/test-runner
                                      {:git/tag "v0.5.1" :git/sha "dfb30dd"}}
                         :main-opts ["-m" "cognitect.test-runner"]
                         :exec-fn cognitect.test-runner.api/test}}}
```

So far we have defined our dependencies (just Clojure for now) and two [aliases](https://practical.li/blog-staging/posts/clojure-cli-tools-understanding-aliases) which will come in handy during development and tests. `dev` alias helps us load the `test` directory during development and `test-runner` helps us run all the tests in our project.

3. Time to add some code. Let's start with `src/core.clj`:
```clj
(ns core)

(defn plus [a b]
  (+ a b))

(defn run [opts]
  (println "Hello world, the sum of 2 and 2 is" (plus 2 2)))
```

And now a basic test `test/core_test.clj`:
```clj
(ns core-test
  (:require [clojure.test :refer [is deftest]]
            [core :refer [plus]]))

(deftest adding-numbers
  (is (= 4 (plus 2 2))))
```

To test that everything above works properly try the following two commands:
```clj
clj -X:test-runner
; ...
; Ran 1 tests containing 1 assertions.
; ...
clj -X core/run
; Hello world, the sum of 2 and 2 is 4
```

### 1.3 Local Development

Personally, I use [VSCode with the Calva Extension](https://clojure.org/guides/editors#_vs_code_rapidly_evolving_beginner_friendly) for local development but you can have your pick from the several options [suggested here](https://clojure.org/guides/editors).


## 2. Setup GitHub Actions

Now that we have a basic project set up, let's setup some basic GitHub actions.

### 2.1 Test on Push

To start with, let's add a workflow to run tests everytime we push to master. Create a file `.github/workflows/test.yaml`:
```yaml
name: Test 🧪

on: [push]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Prepare Java
        uses: actions/setup-java@v3
        with:
          distribution: 'zulu'
          java-version: '17'
      - name: Prepare Clojure
        uses: DeLaGuardo/setup-clojure@10.1
        with:
          cli: 1.11.1.1208
      - name: Run Tests
        run: clojure -X:test-runner
```

### 2.2 Manual Trigger

It might be useful to trigger jobs manually. Let's add a workflow that we can trigger from the GitHub UI which outputs our `hello world`. Create a file `.github/workflow/manual.yaml`:
```yaml
name: Manual 🏃

on: [workflow_dispatch]

jobs:
  manual:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Prepare Java
        uses: actions/setup-java@v3
        with:
          distribution: 'zulu'
          java-version: '17'
      - name: Prepare Clojure
        uses: DeLaGuardo/setup-clojure@10.1
        with:
          cli: 1.11.1.1208
      - name: Run
        run: clojure -X core/run
```