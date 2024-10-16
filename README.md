<!--suppress ALL -->
![licence](https://badgen.net/static/license/Apache%202/gray)
![release](https://img.shields.io/github/v/release/JustDeax/ComposeStopwatch.svg)
![minSdk](https://badgen.net/static/minSdk/24/yellow)

<div align="center">
    <img src="./app/src/main/ic_launcher-playstore.png" width="128" height="128" style="display: block; margin: 0 auto"/>
    <h1>Compose Stopwatch</h1>
    <p>Android stopwatch in the Material You theme, designed for ease of use and best feature</p>
</div>

<div align="center">
    <div style="display: flex; flex-direction: row;">
        <a href='https://f-droid.org/packages/com.justdeax.composeStopwatch/'><img alt='Get it on F-Droid' src='https://fdroid.gitlab.io/artwork/badge/get-it-on.png' style="width:200px"></a>
        <a href='https://github.com/JustDeax/ComposeStopwatch/releases/download/1.8.1/compose-stopwatch.apk'><img alt='Get it on Github' src='https://i.ibb.co.com/16WW8Rm/get-it-on-github.png' style="width:200px"></a>
    </div>
</div>

<p align="center">
  <img src="./metadata/en-US/images/phoneScreenshots/1.png" width="30%" />
  <img src="./metadata/en-US/images/phoneScreenshots/2.png" width="30%" />
  <img src="./metadata/en-US/images/phoneScreenshots/3.png" width="30%" />

  <img src="./metadata/en-US/images/phoneScreenshots/4.png" width="30%" />
  <img src="./metadata/en-US/images/phoneScreenshots/5.png" width="30%" />
</p>

### Compose Stopwatch
Android stopwatch app in the Material You theme, designed for ease of use and best features

- Circular progress **NEW**
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
