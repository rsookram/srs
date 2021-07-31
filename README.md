# SRS

[Spaced Repetition](https://en.wikipedia.org/wiki/Spaced_repetition) Software
(SRS) for Android.


## Features

- Text-based flashcards
- Export saved data to a file on your device (and import later)
- Follows [low-key Anki](https://refold.la/roadmap/stage-1/a/anki-setup/)
- Auto-suspends cards that are known well enough (the next interval is > 1 year)
- Data is stored locally
- Requires no permissions
- Small app (< 2 MB)


## Technologies

- Kotlin
- Kotlin coroutines
- Dagger 2 + Hilt
- Jetpack Compose, Navigation, Paging, and ViewModel
- Accompanist
- SQLDelight
- JUnit 4


## License

```
Copyright 2021 Rashad Sookram

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
