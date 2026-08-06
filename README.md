# Personal Vault — Android Application

Personal Vault is a zero-knowledge, encrypted local storage application built for Android using Kotlin and Jetpack Compose. It securely stores passwords, documents, driving licenses, certificates, ID cards, and bank account information entirely on-device with PBKDF2 key derivation and AES-256-GCM encryption.

## Key Features

- **Zero-Knowledge Architecture**: All data is encrypted locally using AES-256-GCM with keys derived via PBKDF2 (SHA-256).
- **Master Recovery Key**: 16-character generated recovery key to regain access if the master password is forgotten.
- **Vault Sections**:
  - 🔑 **Passwords**: Account credentials, usernames, URLs, categories, favorite tags, and customizable password generator.
  - 📄 **Documents**: Agreements, invoices, receipts, contracts, tags, and description notes.
  - 🚗 **Driving Licenses**: License numbers, holder name, issue & expiry dates, license categories.
  - 🎓 **Certificates**: Academic degrees, professional certificates, institution details, year of completion.
  - 🪪 **ID Cards**: National IDs, passports, voter IDs, card numbers, holder names.
  - 🏦 **Bank Accounts & Cards**: Bank details, routing numbers, account numbers, card numbers, CVV, expiry dates, PINs.
- **Search & Filtering**: Search across all entries instantly, filter by section category or favorites.
- **Encrypted Backup & Restore**: Export and import encrypted JSON backups.
- **Auto-Lock Timeout**: Configurable inactivity lock options (1m, 5m, 15m, Never).

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room Database (KSP)
- **Cryptography**: Java Cryptography Standard (AES-256-GCM, PBKDF2WithHmacSHA256)
- **Architecture**: MVVM with Repository Pattern & Coroutines Flow
