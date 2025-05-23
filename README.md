<!--suppress ALL -->
![licence](https://badgen.net/static/license/Apache%202/gray)
![release](https://img.shields.io/github/v/release/JustDeax/ComposeStopwatch.svg)
![minSdk](https://badgen.net/static/minSdk/26/yellow)

<div align="center">
    <img src="./app/src/main/ic_launcher-playstore.png" width="128" height="128" style="display: block; margin: 0 auto"/>
    <h1>Compose Stopwatch</h1>
    <p>Android stopwatch in the Material You theme, designed for ease of use and best feature</p>
</div>

<div align="center">
    <div style="display: flex; flex-direction: row;">
        <a href='https://f-droid.org/packages/com.justdeax.composeStopwatch/'><img alt='Get it on F-Droid' src='https://fdroid.gitlab.io/artwork/badge/get-it-on.png' style="width:200px"></a>
        <a href='https://github.com/JustDeax/ComposeStopwatch/releases/download/1.8.2/compose-stopwatch.apk'><img alt='Get it on Github' src='https://i.ibb.co.com/16WW8Rm/get-it-on-github.png' style="width:200px"></a>
    </div>
</div>

<p align="center">
  <img src="./metadata/en-US/images/phoneScreenshots/1.png" height="400" />
  <img src="./metadata/en-US/images/phoneScreenshots/2.png" height="400" />
  <img src="./metadata/en-US/images/phoneScreenshots/3.png" height="400" />

  <img src="./metadata/en-US/images/phoneScreenshots/4.png" height="400" />
  <img src="./metadata/en-US/images/phoneScreenshots/5.png" height="400" />
</p>

### Compose Stopwatch
### Notice! if your app version is 1.8.2 or lower, you need to uninstall the old version to update it
<details>
<summary>🔑 APK Signature Key Change Notification (click)</summary>

---

This notice is also available at: https://justdeax.github.io/key-rotation.md

Hello, I am the developer of this application.

Unfortunately, I **lost the signing key** used in previous versions of the app.  
As a result, updates signed with the new key **cannot be installed over existing installs**.  
To continue publishing updates, I have generated a **new signing key**.

**Application ID:** `com.justdeax.composeStopwatch`

**New key (since 2025-05-01):**
```
SHA-256: 6b:2a:b5:9a:56:7e:5e:05:d5:a3:d5:63:66:bd:5a:e0:d1:2a:11:ee:2e:10:46:d5:4d:14:9b:fa:53:43:d2:e0
```

**Old key (before 2025-05-01):**
```
SHA-256: a8:18:9a:88:76:f7:7c:c7:c1:c4:e9:1d:0f:75:30:5a:ba:36:98:8d:9a:48:91:f5:63:c4:a5:dd:a2:2b:70:33
```

I understand that losing a signing key compromises the trust chain.  
To address this:
- I remain in control of this repository and GitHub account
- I am open to further verification steps if required
- I have set up a persistent verification channel to avoid such issues in the future

Thank you for your understanding and continued support.

---

</details>

Android stopwatch app in the Material You theme, designed for ease of use and best features

- Stopwatch Settings
  - Enable Auto Start 3 seconds after app launch with override option
  - Enable vibration for better tactile sensation
  - Select action when you tap on the clock (was before)

- Circular progress
  - Displays the progress of the current lap based on the very first one
  - A dash on the lap progress shows the previous lap

- Change theme, new themes are available
  - Dynamic theme is only available with android 12+
  - Extra Dark theme saves charging for Amoled screen

- Change orientation
  - Portrait mode has more space for laps
  - In Landscape mode, time text is larger

- Select an action when you tap on the clock that shows the time
  - Resume or pause
  - Resume or add lap
  - Resume or pause
  - If you don't want to do this, you can remove the action

- Switching notifications on/off
  - Stopwatch control in notifications
  - When switched off, the stopwatch works on ViewModel and DataStore
  - When enabled, the stopwatch works on LifecycleService and ForegroundService

- Screen Awake mode
  - Ability to watch the stopwatch without the screen falling asleep
