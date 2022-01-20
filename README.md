# Jedlix SDK

Jedlix SDK is part of our smart charging platform.

You can use it to connect a vehicle manufacturer account to a user in the Jedlix [Smart Charging API](https://api.jedlix.com/). It takes a user identifier and access token and presents a view to select a vehicle and authenticate with the manufacturer account.

## Requirements

- Android SDK 21 or higher

## Installation

Add the following to your `build.gradle`:

```
dependencies {
    implementation("com.jedlix:sdk:1.0.0")
}
```

## Usage

When you sign up for a [Smart Charging API](https://api.jedlix.com/) account, you get a custom `baseURL`. You need to provide it to the SDK, as well as a `ConnectSessionObserver` for observing connect session changes and a `TokenProvider` for your access token. Observing the connection session is required because a user might leave the app at any moment. The session identifier should be stored, preferably on a remote location, so the user can restore a session when returning.

Configure the SDK:

```kotlin
import com.jedlix.sdk.JedlixSDK

JedlixSDK.configure(
    /* Base URL of the API */,
    /* Implementation of ConnectSessionObserver */
    /* Implementation of TokenProvider */
    )
```

### API

A limited version of the API is available using `JedlixSDK.api`. You can do requests using:

```kotlin
suspend fun doRequest() {
    when (val response = JedlixSDK.api.request { Users.User(identifier).Vehicles().Get() }) {
        is Api.Response.Success -> // get response.result
        is Api.Response.Failure -> // Handle error
    }
}
```

### Connect session


Connecting to a vehicle manufacturer account can be done through a connect session.

To start a new connect session, register a `JedlixConnectSessionManager` in the `onCreate` of any activity via `registerForJedlixConnectSession` and start it using a user identifier:

```kotlin
class SomeActivity : AppCompatActivity() {

    val userIdentifier = lazy {
        // Get user identifier for the current user
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val connectSessionManager = registerForJedlixConnectSession { result ->
            when (result) {
                is ConnectSessionResult.Finished -> // Session has succesfully been completed
                is ConnectSessionResult.InProgress -> // Session was cancelled prematurely
                is ConnectSessionResult.NotStarted -> // Session failed to start
            }
        }

        findViewById<View>(R.id.some_id).setOnClickListener { connectSessionManager.startConnectSession(userIdentifier) }

    }
}
```

Because a user might leave the app at any moment, the SDK stores the last used session. If you want to store multiple sessions, use the `ConnectSessionResult` to store these values.
Besides starting a new session, the `JedlixConnectSessionManager` can be called using:

- `startConnectSession` Starts a new session (vehicle or home charger) for the user, ignoring any previous sessions. Requires a userIdentifier and `ConnectSession.Settings`
- `startVehicleConnectSession` Starts a new vehicle connect session. Requires a userIdentifier
- `restoreConnectSession` Restores a connect session with a given id. Requires a user identifier and connect session identifier. This should use information observed in the `ConnectSessionObserver`


### Logging

By default the SDK logs only errors. This behaviour can be changed by updating `JedlixSDK.logLevel`:

- `LogLevel.ALL` logs errors and debug logs
- `LogLevel.ERRORS` logs only errors
- `LogLevel.NONE` logs nothing.


## Example

See the included example to learn how to use the SDK.

To run the example, open the project in Android Studio.

- Update the `BASE URL` strings in `ExampleApplication` to the base url of your api
- When using Auth0 for authentication, uncomment `AuthenticationManager.enableAuth0` and replace all strings with the values of your Auth0 domain.

The example can then be deployed to an android device.

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

