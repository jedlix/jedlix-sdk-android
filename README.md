# Jedlix SDK

Jedlix SDK is part of our smart charging platform.

You can use it to connect a vehicle manufacturer account to a user in the Jedlix [Smart Charging API](https://api.jedlix.com/). It takes a user identifier and access token and presents a view to select a vehicle and authenticate with the manufacturer account.

## Requirements

- Android SDK 21+

## Installation

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation("com.jedlix:sdk:1.0.0")
}
```

## Usage

When you sign up for a [Smart Charging API](https://api.jedlix.com/) account, you get a custom `baseURL`. You need to provide it to the SDK, as well as a `ConnectSessionObserver` and `Authentication` implementations.

Configure the SDK:

```kotlin
import com.jedlix.sdk.JedlixSDK

JedlixSDK.configure(
    /* Base URL */,
    /* ConnectSessionObserver implementation */,
    /* Authentication implementation */
)
```

`ConnectSessionObserver` receives a connect session identifier when the session is created and is notified when a session is finished.

```kotlin
interface ConnectSessionObserver {
    fun onConnectSessionCreated(userIdentifier: String, connectSessionIdentifier: String)
    fun onConnectSessionFinished(userIdentifier: String, connectSessionIdentifier: String)
}
```

`Authentication` provides your access token to the SDK. When the token becomes invalid, you can renew it by implementing the `renewAccessToken` function.

```kotlin
interface Authentication {
    suspend fun getAccessToken(): String?
    suspend fun renewAccessToken(): String?
}
```

To start a new connect session, register a `ConnectSessionManager` in the `onCreate` callback of your activity and call `startConnectSession(userIdentifier)`:

```kotlin
class SomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val connectSessionManager = registerConnectSessionManager { result ->
            // continue when ConnectSessionActivity finishes
        }

        findViewById<View>(R.id.some_button).setOnClickListener {
            connectSessionManager.startConnectSession(userIdentifier) 
        }
    }
}
```

Because a user might leave the app at any moment, you should store the session identifier you receive through `ConnectSessionObserver` and continue when they come back using the following function:

```kotlin
connectSessionManager.restoreConnectSession(userIdentifier, connectSessionIdentifier)
```

### Logging

By default the SDK logs only errors. To change it, update `JedlixSDK.logLevel`:

- `LogLevel.ALL` logs errors and debug logs
- `LogLevel.ERRORS` logs only errors
- `LogLevel.NONE` logs nothing

## Example

See the included example to learn how to use the SDK.

Open `ExampleApplication.kt` and specify your `baseURL`:

```kotlin
JedlixSDK.configure(
    URL("<YOUR BASE URL>"),
    ConnectSessionObserver(this),
    Authentication.instance
)
```

(Optional) If you use [Auth0](https://auth0.com/), you can uncomment the following code to authenticate with an Auth0 account directly, assuming the user identifier is stored in JWT body under `userIdentifierKey`.

```kotlin
Authentication.enableAuth0(
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

