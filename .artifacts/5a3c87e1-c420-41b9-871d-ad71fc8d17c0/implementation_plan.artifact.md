# Implementation Plan - Firebase Auth, Web Structure & App Renaming

This plan covers the creation of a `web` directory in the project root, robust integration of Firebase Authentication, and renaming the application to **"Visual Motor"**.

## User Review Required

> [!IMPORTANT]
> **Firebase Console Action**: You must enable the **Email/Password** sign-in provider in your [Firebase Console - Authentication](https://console.firebase.google.com/u/0/project/ai-a32b4/authentication/providers) for the login and signup features to work.

## Proposed Changes

### [Project Structure]

#### [DELETE] [web/](file:///C:/Users/admin/AndroidStudioProjects/pddapp/web/)
- Remove the existing `web` folder to start fresh.

#### [NEW] [web/](file:///C:/Users/admin/AndroidStudioProjects/pddapp/web/)
- Re-create the `web` folder in the project root. This folder will be used for future web-related configurations (e.g., Firebase Hosting, Web landing page).

---

### [Resources]

#### [MODIFY] [strings.xml](file:///C:/Users/admin/AndroidStudioProjects/pddapp/app/src/main/res/values/strings.xml)
- Update `app_name` from "pdd app" to **"Visual Motor"**.

---

### [Android App Logic]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/admin/AndroidStudioProjects/pddapp/app/src/main/java/com/pdd/app/MainActivity.kt)
- **Session Management**:
    - Update `PddApp` to check for an existing Firebase session on startup using `FirebaseAuth.getInstance().currentUser`.
    - If a user is already signed in, skip the `AuthScreen` and navigate directly to the `HomeScreen`.
- **Authentication Flow**:
    - Ensure all Auth logic (Sign Up, Sign In, Forgot Password) uses the `FirebaseAuth` SDK consistently.
    - Implement auto-sync of the user's profile from Firestore immediately after a successful sign-in.
- **Sign Out**:
    - Ensure `FirebaseAuth.getInstance().signOut()` is called correctly, clearing both local and cloud-related app states.

#### [MODIFY] [FirestoreManager.kt](file:///C:/Users/admin/AndroidStudioProjects/pddapp/app/src/main/java/com/pdd/app/database/FirestoreManager.kt)
- Validate that all Firestore calls correctly use the `uid` of the authenticated user.
- Ensure the `getUserProfile` method properly fetches data based on the current user's unique ID.

## Verification Plan

### Manual Verification
1.  **Folder Check**: Verify `C:/Users/admin/AndroidStudioProjects/pddapp/web/` exists.
2.  **App Name**: Verify the launcher icon label is "Visual Motor".
3.  **Sign Up**:
    - Create a new account.
    - Verify that a new user appears in the Firebase Authentication console.
    - Verify that a profile document is created in the `profiles` collection in Firestore.
3.  **Sign In / Auto-Login**:
    - Sign out and sign back in.
    - Close the app and re-open it; verify you are automatically logged in if you didn't sign out.
4.  **Security**:
    - Attempt to access the Home screen without signing in (should be impossible).
