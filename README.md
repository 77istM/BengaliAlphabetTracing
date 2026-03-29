#	Idea
-Tracing accuracy scoring — evaluate how closely the user's strokes follow the guide path

-Progress tracking — remember which letters the user has practiced and show a progress bar

-Animated stroke guidance — animate the correct stroke order step-by-step before the user traces

-Stroke-by-stroke hints — reveal guide strokes one at a time as hints

---
# Bengali Alphabet Tracing — বাংলা বর্ণমালা ট্রেসিং

An interactive ***Android app*** for learning to write the Bengali alphabet through guided tracing exercises (not stroke), with audio pronunciation for every character.

---

## Demo Video

- A short is available at: https://youtube.com/shorts/nPt05Z7ERsM.
- Google Play: //Working towards

---

## Features

### 📚 61 Bengali Characters
The app covers the complete Bengali writing system across four categories:

- **Vowels (স্বরবর্ণ)** — 11 vowel letters
- **Consonants (ব্যঞ্জনবর্ণ)** — 39 consonant letters
- **Numbers (সংখ্যা)** — Bengali numerals 0–10 (11 numerals)
- **All** — all 61 characters in sequence

---

<table>
  <tr>
    <td align="center"><b>Vowels (স্বরবর্ণ)</b></td>
    <td align="center"><b>Consonants (ব্যঞ্জনবর্ণ)</b></td>
    <td align="center"><b>Numbers (সংখ্যা)</b></td>
  </tr>
  <tr>
    <td><img src="asset/vowel.png" width="250" height="auto" /></td>
    <td><img src="asset/consonant.png" width="250" height="auto" /></td>
    <td><img src="asset/number.png" width="250" height="auto" /></td>
  </tr>
</table>

---

### ✍️ Interactive Tracing Canvas
Trace each letter by drawing on the on-screen canvas with your finger:

- A **light grey outline** shows the correct letter shape as a guide.
- Your drawn stroke appears in **green** so you can compare it to the guide.
- Tap **Clear** to erase your attempt and try again.

---

### 🔊 Audio Pronunciation
Every character has a recorded MP3 pronunciation. Tap the **play button** next to the letter to hear it spoken aloud, reinforcing the connection between shape and sound.

---

### 🗂️ Category Selector
Filter the practice session to focus on a specific group:

| Category | Description |
|---|---|
| Vowels / স্বরবর্ণ | অ আ ই ঈ উ ঊ ঋ এ ঐ ও ঔ |
| Consonants / ব্যঞ্জনবর্ণ | ক খ গ ঘ ঙ … and more |
| Numbers / সংখ্যা | ০ ১ ২ ৩ ৪ ৫ ৬ ৭ ৮ ৯ ১০ |
| All | Complete set of 62 characters |

---

## How to Use

1. **Select a category** using the chips at the top of the screen.
2. **Read the letter** shown in the header.
3. **Tap the play button** to hear the pronunciation.
4. **Trace the letter** on the canvas by dragging your finger over the grey guide shape.
5. Tap **Clear** to erase and retry, or use **Next / Previous** to move to another character.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM with ViewModel & StateFlow |
| Design | Material Design 3 |
| Audio | Android MediaPlayer |

