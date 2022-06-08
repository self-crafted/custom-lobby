# custom-lobby

[![standard-readme compliant](https://img.shields.io/badge/standard--readme-OK-green.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)
[![GitHub](https://img.shields.io/github/license/self-crafted/custom-lobby?style=flat-square)](https://github.com/self-crafted/custom-lobby/blob/master/LICENSE)
[![GitHub Repo stars](https://img.shields.io/github/stars/self-crafted/custom-lobby?style=flat-square)](https://github.com/self-crafted/custom-lobby/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/self-crafted/custom-lobby?style=flat-square)](https://github.com/self-crafted/custom-lobby/network/members)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/self-crafted/custom-lobby?style=flat-square)](https://github.com/self-crafted/custom-lobby/releases/latest)
[![GitHub all releases](https://img.shields.io/github/downloads/self-crafted/custom-lobby/total?style=flat-square)](https://github.com/self-crafted/custom-lobby/releases)

custom-lobby is a small minecraft lobby server with [Minestom](https://github.com/Minestom/Minestom) as its core.

custom-lobby is built for my private network, so it may or may not fit your needs.
It includes a map, got only one instance and small gimmicks just for fun.

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [API](#api)
- [Maintainers](#maintainers)
- [Contributing](#contributing)
- [License](#license)

## Install
You could either just download a [release](https://github.com/self-crafted/custom-lobby/releases) or you compile the server yourself using the following commands under Linux
```shell
git clone https://github.com/self-crafted/custom-lobby.git
cd custom-lobby
./gradlew build
```
The server jar will be located at `build/libs/custom-lobby-<VERSION>.jar`.

Note that for compiling you need to use a JDK 17.

## Usage
To run the server you need to have a Java 17 runtime installed.
Use the following command to start the server for the first time.
```shell
java -jar custom-lobby-<VERSION>.jar
```
This generates a `start.sh` script and a settings file with these default values:
```json5
{
  "SERVER_IP": "localhost",
  "SERVER_PORT": 25565,
  "MODE": "OFFLINE", // may be OFFLINE, ONLINE, BUNGEECORD or VELOCITY
  "VELOCITY_SECRET": "",
  "PLAYER_RESTART": false, // make /restart command executable for all players
  "PLAYER_SHUTDOWN": false, // make /shutdown command executable for all players
  "TPS": null, // default 20
  "CHUNK_VIEW_DISTANCE": null, // default 8
  "ENTITY_VIEW_DISTANCE": null, // default 5
  "TERMINAL_DISABLED": "FALSE" // default false
}
```
You have to restart the server for changes in there to take effect.

Note that this server only supports 1.18.2 clients.
You may need to use ViaVersion on the proxy.

## Restarting
Restarting the server calls the `./start.sh` script.
The generated script will restart the server with no way to access the console.
So keep in mind that you will need an extension providing remote access or use tmux/screen in the `start.sh` to access the console.


## API
This server itself does not add some API. But it features [Minestom's API](https://github.com/Minestom/Minestom) so you can use it from within extensions.

## Maintainers

[@off-by-0point5](https://github.com/off-by-0point5)

## Contributing

PRs accepted.

Small note: If editing the README, please conform to the [standard-readme](https://github.com/RichardLitt/standard-readme) specification.

## License

This project is licensed under the [MIT License](LICENSE).
