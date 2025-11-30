# Offline-First Authentication System - Implementation Summary

## ✅ Completed Changes

### 1. User Model Updates (`User.kt`)
- **Roles**: Changed from generic "USER" to `ACTIVE` (paid/synced), `PASSIVE` (24h trial), `ADMIN`
- **24h Trial**: Offline accounts now have 24-hour trial instead of 7 days
- **New Field**: Added `lastSyncTimestamp` to track sync status
- **Helper Functions**:
  - `getUserMode()`: Returns user mode based on role and trial status
  - `needsCleanup()`: Checks if account should be deleted (>24h and unsynced)

### 2. Database Layer (`UserDao.kt`)
- **New Queries**:
  - `deleteExpiredUnsyncedUsers()`: Delete accounts older than 24h that haven't synced
  - `getExpiredUnsyncedUsers()`: Get list of expired accounts for logging

### 3. Repository Layer (`AuthRepository.kt`)
- **Offline Registration**: Now creates PASSIVE role with 24h trial
- **Cleanup Function**: `cleanExpiredOfflineAccounts()` - Deletes expired unsynced accounts
- **Enhanced Logging**: All auth operations now log to Firebase Crashlytics

### 4. ViewModel Layer (`AuthViewModel.kt`)
- **Startup Cleanup**: Runs `cleanExpiredOfflineAccounts()` on initialization
- **Login/Register**: Already had logging, now properly integrated

### 5. UI Layer (`LoginScreen.kt`)
- **Removed**: "Continuer en tant qu'invité" button (guest login)
- **Fixed**: Login button now actually calls `viewModel.login()` instead of being a TODO

### 6. Sync System (NEW)
- **`UserSyncWorker.kt`**: Background worker that:
  - Checks for PENDING_CREATE users
  - Uploads metadata to Firebase (no passwords)
  - Promotes users from PASSIVE → ACTIVE on successful sync
  - Removes trial expiry after sync
- **`SyncManager.kt`**: Manages sync scheduling:
  - Periodic sync every 6 hours (when online)
  - Immediate sync trigger (e.g., after registration)
  - Network-aware (only runs when connected)

### 7. Application Initialization (`BacXApplication.kt`)
- **Sync Scheduler**: Automatically schedules periodic sync on app startup
- **Dependency Injection**: SyncManager properly injected via Hilt

## 📋 How It Works

### Registration Flow (Offline)
1. User fills registration form
2. Payment simulation (currently mocked)
3. Account created with:
   - `role = "PASSIVE"`
   - `syncStatus = "PENDING_CREATE"`
   - `trialExpiresAt = now + 24h`
4. User can access app with limited features for 24 hours

### Sync Flow
1. App starts → cleanup runs (deletes expired accounts)
2. SyncWorker runs periodically (every 6h) when online
3. Worker finds PENDING_CREATE users
4. Uploads metadata to Firebase
5. Updates local user:
   - `role = "ACTIVE"`
   - `syncStatus = "SYNCED"`
   - `trialExpiresAt = null` (unlimited)

### Login Flow
1. User enters pseudo + 4-digit code
2. System checks local database
3. Validates password hash
4. Returns user with role (ACTIVE/PASSIVE/ADMIN)
5. UI adjusts features based on role

## 🔄 Remaining TODOs (For Production)

### High Priority
1. **Register Screen**: Update to set `role = "ACTIVE"` for online registrations with successful payment
2. **Admin Code**: Implement admin activation logic
3. **UI Feedback**: Show sync status banner ("Account pending sync")
4. **Feature Gating**: Limit PASSIVE user access to specific features

### Medium Priority
5. **Retry Logic**: Handle failed syncs more gracefully
6. **Conflict Resolution**: Handle case where user registers on multiple devices
7. **Migration**: Create Room migration to add new `lastSyncTimestamp` field
8. **Testing**: Unit tests for sync worker and cleanup logic

### Low Priority
9. **Analytics**: Track PASSIVE → ACTIVE conversion rate
10. **Notification**: Alert user when trial is about to expire
11. **Manual Sync**: Add "Sync Now" button in settings

## 🎯 Key Benefits

1. **Offline-First**: Users can register and use app without internet
2. **Security**: No passwords uploaded to server (only metadata)
3. **Automatic**: Sync happens in background, no user action needed
4. **Clean Database**: Expired trials auto-delete after 24h
5. **Scalable**: WorkManager handles retry and backoff automatically

## 📊 User States

| State | Role | Sync Status | Trial | Access Level |
|-------|------|-------------|-------|--------------|
| Fresh Install | - | - | - | Login/Register only |
| Offline Trial | PASSIVE | PENDING_CREATE | 24h | Limited features |
| Synced User | ACTIVE | SYNCED | None | Full access |
| Admin | ADMIN | SYNCED | None | Full + Admin panel |
| Expired Trial | PASSIVE | PENDING_CREATE | Expired | Blocked (deleted on cleanup) |

## 🔒 Security Notes

- Passwords are SHA-256 hashed before storage
- Only metadata synced to Firebase (no private data)
- Guest mode removed (all users must have account)
- 3 account limit per device enforced
