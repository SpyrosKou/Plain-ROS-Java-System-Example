# High-Level Changelog

This file summarizes the committed project history at a high level. It focuses on major feature additions, build or dependency shifts, and long quiet periods where the only notable movement was tooling or documentation maintenance.

- 2020-05-15: Initial repository setup with the project README, license, and ignore rules.
- 2020-06-19: First runnable standalone rosjava example landed with the Gradle application build, wrapper scripts, the main launcher, and ROS topic publisher/subscriber nodes. The Gradle wrapper moved from `6.4.1` to `6.5` the same day.
- 2020-06-21: Small cleanup pass with license and ignore updates, minor code polish, and clearer README guidance around using the Gradle wrapper.
- 2020-06-21 to 2021-07-06: Quiet period of 380 days. Only a README typo fix landed during this stretch.
- 2021-07-06 to 2022-01-21: Quiet period of 199 days. The next update was a maintenance refresh: dependencies were updated, the documented tested JDK moved from `14` to `17`, and Gradle moved from `6.5` to `7.3.3`.
- 2022-06-21: Tooling update only. Gradle moved from `7.3.3` to `7.4.2`.
- 2022-06-22: Major feature expansion. The example set grew from topic pub/sub to include a ROS service server and a ROS service client, with the required extra rosjava message dependency.
- 2022-06-24: Added the external-roscore workflow example and a dedicated Gradle task for running rosjava against an already running non-rosjava ROS master.
- 2022-07-19: Tooling update only. Gradle moved from `7.4.2` to `7.5`.
- 2022-08-31: Small maintenance and documentation update. Gradle moved from `7.5` to `7.5.1`, and the README added a link to the related custom ROS messages example repository.
- 2022-08-31 to 2026-03-30: Quiet period of 1307 days. The next committed change was a maintenance revival rather than a new feature wave.
- 2026-03-30: Dependency and build refresh. Gradle moved from `7.5.1` to `8.14.4`, the rosjava dependency moved to `0.4.1.1`, `message_generation` moved to `0.3.9`, `commons-configuration2` moved to `2.10.1`, `commons-lang3` moved to `3.18.0`, and the project switched to a new GitHub-hosted Maven repository.

## Toolchain Timeline

- Gradle: `6.4.1` on 2020-06-19 -> `6.5` on 2020-06-19 -> `7.3.3` on 2022-01-21 -> `7.4.2` on 2022-06-21 -> `7.5` on 2022-07-19 -> `7.5.1` on 2022-08-31 -> `8.14.4` on 2026-03-30.
- Java: The documented tested JDK in `README.md` moved from `14` to `17` on 2022-01-21. No committed Gradle toolchain or `sourceCompatibility` setting was added in `build.gradle`, so this change is documented rather than build-enforced.
