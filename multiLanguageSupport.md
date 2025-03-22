### How does LanguageTool support texts in multiple languages?

Starting with Version 6.4 LanguageTool improves the handling of texts written in multiple languages.

We currently set the language for a text before checking, or it can be set by the user in the add-on.

This means that individual sentences that differ from the main language have many matches, even though they contain no errors. Or errors are not recognized because the correct language for these sentences was not used during checking.

And right here, we have now added a mechanism that allows sentences with too many spelling errors to be checked again by our languageDetectionService.

The previously founded errors remain in the CheckResults object. Additionally, we put the new ExtendedSentenceRange data in CheckResults object.

For example, the text:
> Das ist Deutsch. This is english.

The corresponding request to the API would be:
> https://languagetool.org/v2/check?text=Das%20ist%20Deutsch.%20This%20is%20english.&language=auto&&preferredLanguages=de,en

Our server will detect this text as German and send this response:

```json
{
  "software":{
    "name":"LanguageTool",
    "version":"6.4-SNAPSHOT",
    "buildDate":null,
    "apiVersion":1,
    "premium":false,
    "status":""
  },
  "warnings":{
    "incompleteResults":false
  },
  "language":{
    "name":"German (Germany)",
    "code":"de-DE",
    "detectedLanguage":{
      "name":"German (Germany)",
      "code":"de-DE",
      "confidence":0.99775517,
      "source":"ngram+prefLang(forced: false)"
    }
  },
  "matches":[
    {
      "message":"Möglicher Tippfehler gefunden.",
      "shortMessage":"Rechtschreibfehler",
      "replacements":[
        {
          "value":"Typ"
        },
        {
          "value":"Taz"
        },
        {
          "value":"Typs"
        },
        {
          "value":"Die"
        },
        {
          "value":"Tim"
        },
        {
          "value":"Des"
        },
        {
          "value":"Tims"
        },
        {
          "value":"Das"
        },
        {
          "value":"Thais"
        },
        {
          "value":"Bis"
        },
        {
          "value":"TVs"
        },
        {
          "value":"Dass"
        },
        {
          "value":"TTs"
        },
        {
          "value":"Dies"
        },
        {
          "value":"TiB"
        },
        {
          "value":"Hin"
        },
        {
          "value":"Theiß"
        },
        {
          "value":"Teil"
        },
        {
          "value":"Thies"
        },
        {
          "value":"Dahin"
        }
      ],
      "offset":17,
      "length":4,
      "context":{
        "text":"Das ist Deutsch. This is english.",
        "offset":17,
        "length":4
      },
      "sentence":"This is english.",
      "type":{
        "typeName":"UnknownWord"
      },
      "rule":{
        "id":"GERMAN_SPELLER_RULE",
        "description":"Möglicher Rechtschreibfehler",
        "issueType":"misspelling",
        "category":{
          "id":"TYPOS",
          "name":"Mögliche Tippfehler"
        }
      },
      "ignoreForIncompleteSentence":false,
      "contextForSureMatch":0
    },
    {
      "message":"Möglicher Tippfehler gefunden.",
      "shortMessage":"Rechtschreibfehler",
      "replacements":[
        {
          "value":"ist"
        },
        {
          "value":"IS"
        },
        {
          "value":"die"
        },
        {
          "value":"in"
        },
        {
          "value":"im",
          "shortDescription":"Positionsangabe"
        },
        {
          "value":"mit"
        },
        {
          "value":"ein"
        },
        {
          "value":"bis"
        },
        {
          "value":"es"
        },
        {
          "value":"sie"
        },
        {
          "value":"wie"
        },
        {
          "value":"ihm",
          "shortDescription":"Dativ von 'er'"
        },
        {
          "value":"ihn"
        },
        {
          "value":"ihr"
        },
        {
          "value":"ins"
        },
        {
          "value":"hin"
        },
        {
          "value":"ich"
        },
        {
          "value":"nie"
        },
        {
          "value":"wir"
        },
        {
          "value":"bin"
        }
      ],
      "offset":22,
      "length":2,
      "context":{
        "text":"Das ist Deutsch. This is english.",
        "offset":22,
        "length":2
      },
      "sentence":"This is english.",
      "type":{
        "typeName":"UnknownWord"
      },
      "rule":{
        "id":"GERMAN_SPELLER_RULE",
        "description":"Möglicher Rechtschreibfehler",
        "issueType":"misspelling",
        "category":{
          "id":"TYPOS",
          "name":"Mögliche Tippfehler"
        }
      },
      "ignoreForIncompleteSentence":false,
      "contextForSureMatch":0
    },
    {
      "message":"Möglicher Tippfehler gefunden.",
      "shortMessage":"Rechtschreibfehler",
      "replacements":[
        {
          "value":"englisch"
        },
        {
          "value":"englische"
        },
        {
          "value":"endlich"
        },
        {
          "value":"englisch-"
        },
        {
          "value":"entlieh"
        },
        {
          "value":"Anglist"
        },
        {
          "value":"Denglisch"
        },
        {
          "value":"Englisch"
        },
        {
          "value":"anglich"
        },
        {
          "value":"anglisch"
        },
        {
          "value":"enolisch"
        },
        {
          "value":"denglisch"
        }
      ],
      "offset":25,
      "length":7,
      "context":{
        "text":"Das ist Deutsch. This is english.",
        "offset":25,
        "length":7
      },
      "sentence":"This is english.",
      "type":{
        "typeName":"UnknownWord"
      },
      "rule":{
        "id":"GERMAN_SPELLER_RULE",
        "description":"Möglicher Rechtschreibfehler",
        "issueType":"misspelling",
        "category":{
          "id":"TYPOS",
          "name":"Mögliche Tippfehler"
        }
      },
      "ignoreForIncompleteSentence":false,
      "contextForSureMatch":0
    }
  ],
  "ignoreRanges":[
    {
      "from":17,
      "to":33,
      "language":{
        "code":"en"
      }
    }
  ],
  "sentenceRanges":[
    [
      0,
      16
    ],
    [
      17,
      33
    ]
  ],
  "extendedSentenceRanges":[
    {
      "from":0,
      "to":16,
      "detectedLanguages":[
        {
          "language":"de",
          "rate":1
        }
      ]
    },
    {
      "from":17,
      "to":33,
      "detectedLanguages":[
        {
          "language":"en",
          "rate":1
        },
        {
          "language":"de",
          "rate":0
        }
      ]
    }
  ]
}
```

We already have ignoreRanges and sentenceRanges in the response. The extendedSentenceRanges are an extended combination of both.
- one extendedSentenceRanges for each sentence (will replace the sentenceRanges)
- at least one detected language per sentence (will replace the ignoreRanges → if the detected language does not equal the main language)

IMPORTANT: The additional language detection for each sentence only works if the user has at least 2 preferredLanguages and the detected language of a sentence is one of the preferredLanguages.

Client applications can use this information to trigger a new check for each sentence that differs from the main language.