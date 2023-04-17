# Jedlix SDK

Jedlix SDK is part of our smart charging platform.

You can use it to connect a vehicle or charger manufacturer account to a user in the Jedlix [Smart Charging API](https://api.jedlix.com/). It presents a view to select a vehicle or charger and authenticate with the manufacturer account.

## Requirements

- Android SDK 21+

## Installation

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation("com.jedlix:sdk:1.5.0")
}
```

## Usage

When you sign up for a [Smart Charging API](https://api.jedlix.com/) account, you get a custom `baseURL` and `apiKey`. Configure the SDK with these values and an `Authentication` implementation. API key is not required if you use your own base URL.

```kotlin
import com.jedlix.sdk.JedlixSDK

JedlixSDK.configure(
    /* Base URL */,
    /* API key */,
    /* Authentication implementation */
)
```

`Authentication` provides your access token to the SDK. When the token becomes invalid, you should renew it before returning in `getAccessToken`.

```kotlin
interface Authentication {
    suspend fun getAccessToken(): String?
}
```

To start a vehicle connect session, register a `ConnectSessionManager` in the `onCreate` callback of your activity and call `startConnectSession(userIdentifier, ConnectSessionType.Vehicle)`:

```kotlin
class SomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val connectSessionManager = registerConnectSessionManager { result ->
            // continue when ConnectSessionActivity finishes
        }

        findViewById<View>(R.id.some_button).setOnClickListener {
            connectSessionManager.startConnectSession(
                "<USER ID>",
                ConnectSessionType.Vehicle
            )
        }
    }
}
```

To start a connect session for a selected vehicle, you need to specify the vehicle identifier:

```kotlin
connectSessionManager.startConnectSession(
    "<USER ID>",
    ConnectSessionType.SelectedVehicle("<VEHICLE ID>")
)
```

To start a charger connect session, you need to specify a charging location identifier:

```swift
connectSessionManager.startConnectSession(
    "<USER ID>",
    ConnectSessionType.Charger("<CHARGING LOCATION ID>")
)
```

A user might leave the app at any moment. If the connect session hasn't been finished, you should resume by providing a session identifier you obtain from the Smart Charging API:

```kotlin
connectSessionManager.resumeConnectSession(
    "<USER ID>",
    "<CONNECT SESSION ID>"
)
```

### Logging

By default the SDK logs only errors. To change it, update `JedlixSDK.logLevel`:

- `LogLevel.ALL` logs errors and debug logs
- `LogLevel.ERRORS` logs only errors
- `LogLevel.NONE` logs nothing

## Example

See the included example to learn how to use the SDK.

Open `ExampleApplication.kt` and specify your `baseURL` and `apiKey`:

```kotlin
baseURL = URL("<YOUR BASE URL>")
apiKey = "<YOUR API KEY>"
```

(Optional) If you use [Auth0](https://auth0.com/), you can uncomment the following code to authenticate with an Auth0 account directly, assuming the user identifier is stored in JWT body under `userIdentifierKey`.

```kotlin
authentication = Auth0Authentication(
    "<AUTH0 CLIENT ID>",
    "<AUTH0 DOMAIN>",
    "<AUTH0 AUDIENCE>",
    "<USER IDENTIFIER KEY>",
    coroutineScope,
    this
)
```

## Documentation

You can find documentation and learn more about our APIs at [api.jedlix.com](https://api.jedlix.com)

## Contact

To set up an account, please contact us at [jedlix.zendesk.com](https://jedlix.zendesk.com/hc/en-us/requests/new)

## License

```markdown
Copyright 2022 Jedlix B.V.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

