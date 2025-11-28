import Foundation
import CoreLocation
import os.log

@objc public class CapgoCompass: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let log = OSLog(subsystem: "app.capgo.capacitor.compass", category: "CapgoCompass")

    private var lastTrueHeading: Double = -1.0
    private var headingCallback: ((Double) -> Void)?
    private var permissionCallback: (() -> Void)?

    @objc override public init() {
        super.init()
        locationManager.delegate = self
    }

    @objc public func requestPermission(completion: @escaping () -> Void) {
        permissionCallback = completion
        locationManager.requestWhenInUseAuthorization()
    }

    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        // Only call the callback if permission was just requested
        if let callback = permissionCallback {
            callback()
            permissionCallback = nil
        }
    }

    @objc public func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        self.lastTrueHeading = newHeading.trueHeading
        headingCallback?(newHeading.trueHeading)
    }

    @objc public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        os_log("Location manager failed with error: %{public}@", log: log, type: .error, error.localizedDescription)
    }

    @objc public func startListeners() {
        if CLLocationManager.headingAvailable() {
            locationManager.startUpdatingLocation()
            locationManager.startUpdatingHeading()
        } else {
            os_log("CLLocationManager heading not available", log: log, type: .error)
        }
    }

    @objc public func stopListeners() {
        locationManager.stopUpdatingLocation()
        locationManager.stopUpdatingHeading()
    }

    @objc public func getCurrentHeading() -> Double {
        return self.lastTrueHeading
    }

    @objc public func setHeadingCallback(_ callback: ((Double) -> Void)?) {
        self.headingCallback = callback
    }

    public func getAuthorizationStatus() -> CLAuthorizationStatus {
        if #available(iOS 14.0, *) {
            return locationManager.authorizationStatus
        } else {
            return CLLocationManager.authorizationStatus()
        }
    }
}
