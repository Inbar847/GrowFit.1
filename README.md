GrowFit – Android Fitness Tracker

A small, clean Android app (Kotlin + Jetpack Compose + Firebase) to track workouts. It has auth, a weekly plan, a workout journal (with optional photos), and a simple progress chart — all with a soft green Material 3 theme.



Features

Email/password Sign in / Sign up, auto-login, Logout.

Plan: weekly plan (add / edit / delete).

Journal: log workouts (type, duration, weight, notes) + optional photo.

Progress: line chart by exercise, metric (max weight / total duration), and days (7/30/90).

Nice UX: Material 3, Navigation, empty states, form validation, RTL.

Quick start

Open the project in Android Studio (Koala/Iguana+), JDK 17.

Put your Firebase app/google-services.json (package must match, e.g. com.example.growfit1).

Sync Gradle → Run ▶ app (API 30+ device/emulator).

Firebase (once)

Enable Authentication → Email/Password.

Firestore Rules (publish):

rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {
    match /users/{uid}/{doc=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}


(Optional photos) Storage Rules:

rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /journal/{uid}/{all=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}


Note: Storage may require the Blaze plan. App saves entries even without a photo.

How to use

Create account or Sign in.

Plan → add/edit/delete items.

Track → fill workout (+ optional photo) → Save.

Journal → view/edit/delete.

Progress → pick Exercise/Metric/Days (↻ to refresh).

Logout from the top bar.

Troubleshooting

PERMISSION_DENIED: publish the rules above & make sure you’re logged in.

Storage errors: Storage disabled/Blaze needed; photos are optional.

JVM 1.8 vs 17: set kotlinOptions.jvmTarget = "17" and Java 17 in compileOptions.

License: MIT (or leave unlicensed for coursework).
