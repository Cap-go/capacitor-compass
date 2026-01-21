# @capgo/capacitor-compass
 <a href="https://capgo.app/"><img src='https://raw.githubusercontent.com/Cap-go/capgo/main/assets/capgo_banner.png' alt='Capgo - Instant updates for capacitor'/></a>

<div align="center">
  <h2><a href="https://capgo.app/?ref=plugin_compass"> ➡️ Get Instant updates for your App with Capgo</a></h2>
  <h2><a href="https://capgo.app/consulting/?ref=plugin_compass"> Missing a feature? We'll build the plugin for you 💪</a></h2>
</div>

Native compass heading plugin for Capacitor.

## Why Capacitor Compass?

The official Capacitor [Motion API](https://capacitorjs.com/docs/apis/motion) relies on web APIs for compass/heading data, which provides a suboptimal developer experience:

- **Inconsistent behavior** across platforms
- **Additional permissions handling** through web APIs
- **Limited accuracy** compared to native implementations
- **Poor performance** on some devices

This plugin provides **true native compass functionality** using:

- **iOS**: `CLLocationManager` for accurate heading data via CoreLocation
- **Android**: Hardware sensors (accelerometer + magnetometer) for precise bearing calculation
- **Event-based API**: Modern `addListener` pattern for real-time heading updates

Essential for navigation apps, augmented reality, location-based games, and any app needing accurate compass heading.

## Install

```bash
npm install @capgo/capacitor-compass
npx cap sync
```

## Requirements

### iOS

Add the following to your `Info.plist`:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>We need access to your location to determine compass heading</string>
```

### Android

No additional setup required. The plugin uses the device's accelerometer and magnetometer sensors.

## Usage

```typescript
import { CapgoCompass } from '@capgo/capacitor-compass';

// Get current heading once
const { value } = await CapgoCompass.getCurrentHeading();
console.log('Current heading:', value, 'degrees');

// Listen for continuous heading updates
const handle = await CapgoCompass.addListener('headingChange', (event) => {
  console.log('Heading:', event.value, 'degrees');
});

// Start the compass sensor
await CapgoCompass.startListening();

// Later: stop listening
await CapgoCompass.stopListening();
await handle.remove();
```

## API

<docgen-index>

* [`getCurrentHeading()`](#getcurrentheading)
* [`getPluginVersion()`](#getpluginversion)
* [`startListening(...)`](#startlistening)
* [`stopListening()`](#stoplistening)
* [`addListener('headingChange', ...)`](#addlistenerheadingchange-)
* [`removeAllListeners()`](#removealllisteners)
* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Capacitor Compass Plugin interface for reading device compass heading.

### getCurrentHeading()

```typescript
getCurrentHeading() => Promise<CompassHeading>
```

Get the current compass heading in degrees.
On iOS, the heading is updated in the background, and the latest value is returned.
On Android, the heading is calculated when the method is called using accelerometer and magnetometer sensors.
Not implemented on Web.

**Returns:** <code>Promise&lt;<a href="#compassheading">CompassHeading</a>&gt;</code>

**Since:** 7.0.0

--------------------


### getPluginVersion()

```typescript
getPluginVersion() => Promise<{ version: string; }>
```

Get the native Capacitor plugin version.

**Returns:** <code>Promise&lt;{ version: string; }&gt;</code>

**Since:** 7.0.0

--------------------


### startListening(...)

```typescript
startListening(options?: ListeningOptions | undefined) => Promise<void>
```

Start listening for compass heading changes via events.
This starts the compass sensors and emits 'headingChange' events.

| Param         | Type                                                          | Description                                      |
| ------------- | ------------------------------------------------------------- | ------------------------------------------------ |
| **`options`** | <code><a href="#listeningoptions">ListeningOptions</a></code> | Optional configuration for throttling behavior |

**Since:** 7.0.0

--------------------


### stopListening()

```typescript
stopListening() => Promise<void>
```

Stop listening for compass heading changes.
This stops the compass sensors and stops emitting events.

**Since:** 7.0.0

--------------------


### addListener('headingChange', ...)

```typescript
addListener(eventName: 'headingChange', listenerFunc: (event: HeadingChangeEvent) => void) => Promise<{ remove: () => Promise<void>; }>
```

Add a listener for compass events.

| Param              | Type                                                                                  | Description                                      |
| ------------------ | ------------------------------------------------------------------------------------- | ------------------------------------------------ |
| **`eventName`**    | <code>'headingChange'</code>                                                          | - The event to listen for ('headingChange')      |
| **`listenerFunc`** | <code>(event: <a href="#headingchangeevent">HeadingChangeEvent</a>) =&gt; void</code> | - The function to call when the event is emitted |

**Returns:** <code>Promise&lt;{ remove: () =&gt; Promise&lt;void&gt;; }&gt;</code>

**Since:** 7.0.0

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all listeners for this plugin.

**Since:** 7.0.0

--------------------


### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

Check the current permission status for accessing compass data.
On iOS, this checks location permission status.
On Android, this always returns 'granted' as no permissions are required.

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

**Since:** 7.0.0

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<PermissionStatus>
```

Request permission to access compass data.
On iOS, this requests location permission (required for heading data).
On Android, this resolves immediately as no permissions are required.

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

**Since:** 7.0.0

--------------------


### Interfaces


#### CompassHeading

Result containing the compass heading value.

| Prop        | Type                | Description                        |
| ----------- | ------------------- | ---------------------------------- |
| **`value`** | <code>number</code> | Compass heading in degrees (0-360) |


#### ListeningOptions

Options for configuring compass listening behavior.

| Prop                   | Type                | Description                                                                                                                                                       | Default          | Since |
| ---------------------- | ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------- | ----- |
| **`minInterval`**      | <code>number</code> | Minimum interval between heading change events in milliseconds. Lower values = more frequent updates but higher CPU/battery usage.                                | <code>100</code> | 8.1.4 |
| **`minHeadingChange`** | <code>number</code> | Minimum heading change in degrees required to trigger an event. Lower values = more sensitive but more events. Handles wraparound (e.g., 359° to 1° = 2° change). | <code>2.0</code> | 8.1.4 |


#### HeadingChangeEvent

Event data for heading change events.

| Prop        | Type                | Description                        |
| ----------- | ------------------- | ---------------------------------- |
| **`value`** | <code>number</code> | Compass heading in degrees (0-360) |


#### PermissionStatus

Permission status for compass plugin.

| Prop          | Type                                                        | Description                                                                                                                                                                             | Since |
| ------------- | ----------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| **`compass`** | <code><a href="#permissionstate">PermissionState</a></code> | Permission state for accessing compass/location data. On iOS, this requires location permission to access heading. On Android, no special permissions are required for compass sensors. | 7.0.0 |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>

</docgen-api>

## Credits

This plugin is inspired by [capacitor-native-compass](https://github.com/HeyItsBATMAN/capacitor-native-compass) by HeyItsBATMAN.
