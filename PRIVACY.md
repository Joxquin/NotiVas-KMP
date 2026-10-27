# Privacy Policy for NotiVas

Last updated: October 27, 2026

This Privacy Policy describes how NotiVas ("we", "our", or "the application") collects, uses, and protects information when you use the NotiVas multiplatform application.

## 1. Information Collection and Storage

NotiVas is designed as a client-side tool to assist students and educators with academic tasks and schedule management.

- **Local Storage**: All your academic credentials, schedule data, forum posts, cached assignments, and application preferences are stored locally on your device using secure local storage mechanisms.
- **No Third-Party Analytics**: NotiVas does not sell, rent, or monetize your personal information or academic data.

## 2. Google Drive and Google API Services

NotiVas provides optional integration with Google Drive to enable features such as creating, reading, and syncing assignment drafts and templates.

### A. Data Accessed via Google APIs
When you connect your Google account, NotiVas requests access to:
- View your email address (`https://www.googleapis.com/auth/userinfo.email`): Used strictly to identify the currently connected account within the app.
- Manage and access files created by NotiVas (`https://www.googleapis.com/auth/drive.file`): Used to create, edit, and organize assignment documents in your Google Drive.
- Read files (`https://www.googleapis.com/auth/drive.readonly`): Used to inspect relevant assignment templates or preview drafts that you have selected.

### B. Google API Services User Data Policy Compliance
NotiVas's use and transfer to any other app of information received from Google APIs will adhere to the [Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy), including the Limited Use requirements.

- We do not transfer your Google user data to external servers or third parties.
- Tokens obtained through Google OAuth are stored securely on your local device (such as Android EncryptedSharedPreferences / Keychain) and are solely transmitted directly to official Google API endpoints.
- We do not use Google user data for advertising, machine learning model training without consent, or profiling.

## 3. Account Unlinking and Data Deletion

You can disconnect your Google account and revoke access at any time:
- Within the NotiVas settings by selecting "Unlink" or "Sign out" from Google Drive.
- Through your Google Account settings under [Third-party apps with account access](https://myaccount.google.com/permissions).

Disconnecting immediately removes all stored OAuth access and refresh tokens from your local device.

## 4. Third-Party Services

When using AI-assisted features (such as Copilot), prompts and context are processed directly using configured official API providers (such as Google Gemini API). No personal identifying details are shared beyond what is strictly necessary to complete your prompt request.

## 5. Changes to This Privacy Policy

We may update this Privacy Policy periodically to reflect enhancements in the application. Any updates will be published directly in the official project repository.

## 6. Contact

If you have questions or concerns regarding this Privacy Policy or data handling practices in NotiVas, please open an issue in the official project repository:
- Project Repository: [https://github.com/Joxquin/NotiVas-KMP](https://github.com/Joxquin/NotiVas-KMP)
- Contact Email: jotajoxquin@gmail.com
