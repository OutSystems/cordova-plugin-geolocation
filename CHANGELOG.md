# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.0.1-OS13]
### Fixes
- Fix on Android to stop location updates when watch times out quickly [RMET-2621](https://outsystemsrd.atlassian.net/browse/RMET-2621)

## [4.0.1-OS12]
### Fixes
- Fix on Android to return an error if the user denies to enable the location when prompted by the plugin [RMET-3539](https://outsystemsrd.atlassian.net/browse/RMET-3539)

## [4.0.1-OS11]
### Fixes
- Fixed Android MABS 10 build by removing android support libraries [RMET-2834](https://outsystemsrd.atlassian.net/browse/RMET-2834)

## [4.0.1-OS10]
- New plugin release to include metadata tag for MABS 7.2.0 compatibility

## [4.0.1-OS9]
### Fixes
- Fixed Android AddWatch deny permissions crash [RMET-964](https://outsystemsrd.atlassian.net/browse/RMET-964)
- Fixed onRequestPermissionResult method to accept the approximate location option [RMET-823](https://outsystemsrd.atlassian.net/browse/RMET-823)

## [4.0.1-OS8]
- Fixed GetLocation for iOS to return a number instead of a string [RMET-918](https://outsystemsrd.atlassian.net/browse/RMET-918)

## [4.0.1-OS7]
### Fixes
- Fixed requestLocationUpdatesIfSettingsSatisfied method to show the user a dialog to enable location or change location settings [RMET-608](https://outsystemsrd.atlassian.net/browse/RMET-608)
- Fixed checkGooglePlayServicesAvailable method to show the user an error dialog when the error can be resolved [RMET-609](https://outsystemsrd.atlassian.net/browse/RMET-609)
- Fixed error in iOS: Round up location value to max 15 digits after decimal point [RMET-522](https://outsystemsrd.atlassian.net/browse/RMET-522)

## [4.0.1-OS3]
### Fixes
- Method getLocation is no longer called on cancelled permissions request [RNMT-4280](https://outsystemsrd.atlassian.net/browse/RNMT-4280)

## [4.0.1-OS2]

### Additions

- Add new native implementation on Android [RNMT-2811](https://outsystemsrd.atlassian.net/browse/RNMT-2811)

## [4.0.1-OS1]

### Additions
- Adds NSLocationAlwaysUsageDescription preference to plugin.xml [RNMT-2651](https://outsystemsrd.atlassian.net/browse/RNMT-2651)

## [4.0.1-OS]
- Fix to allow 4.0.0 version install [CB-13705](https://issues.apache.org/jira/browse/CB-13705)

[Unreleased]: https://github.com/OutSystems/cordova-plugin-geolocation/compare/4.0.1-OS...HEAD
[4.0.1-OS3]: https://github.com/OutSystems/cordova-plugin-geolocation/compare/4.0.1-OS2...4.0.1-OS3
[4.0.1-OS2]: https://github.com/OutSystems/cordova-plugin-geolocation/compare/4.0.1-OS1...4.0.1-OS2
[4.0.1-OS1]: https://github.com/OutSystems/cordova-plugin-geolocation/compare/4.0.1-OS...4.0.1-OS1
[4.0.1-OS]: https://github.com/OutSystems/cordova-plugin-geolocation/compare/4.0.1...4.0.1-OS