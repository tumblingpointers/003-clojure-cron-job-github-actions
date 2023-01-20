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