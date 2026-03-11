
## Instructions to follow the Workshop

Instructions: https://docs.google.com/document/d/1QJS9MUaofSC1RfGFxzJVprY25D2S4c3a631HJRSiKEY/edit?usp=sharing

Slides: https://docs.google.com/presentation/d/1MCuynF-6yunOLyFz5y9WTeFj9_np8ZWR/edit?usp=sharing&ouid=117579161492281663932&rtpof=true&sd=true

Model: https://www.kaggle.com/models/google/gemma-2/tfLite

## Setup

1. Clone the `main` branch, 

```bash
git clone https://github.com/shivay-couchbase/couchbase-lite-workshop.git
```

2. If `local.properties` doesn't exist, the build will use a default empty API key. To use the Gemini API, make sure to add your actual API key to `local.properties`.

If you don't have a `local.properties` file, create one in the root directory of the project and add your Gemini API key:
```
geminiKey="YOUR_ACTUAL_API_KEY_HERE"
```
