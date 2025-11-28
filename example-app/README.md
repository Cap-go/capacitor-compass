# Example App for `@capgo/capacitor-compass`

This Vite project links directly to the local plugin source so you can exercise the native APIs while developing.

## Actions in this playground

- **Get current heading** – Calls getCurrentHeading() to get the current compass heading in degrees (0-360).
- **Start listening** – Starts listening for compass heading changes with live updates via addListener.
- **Stop listening** – Stops listening for compass heading changes.
- **Get plugin version** – Returns the native plugin version.
- **Check permissions** – Checks the current permission status for compass access.
- **Request permissions** – Requests permission to access compass data (iOS requires location permission).

## Getting started

```bash
npm install
npm start
```

Add native shells with `npx cap add ios` or `npx cap add android` from this folder to try behaviour on device or simulator.
