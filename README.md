# custom-lobby

[![standard-readme compliant](https://img.shields.io/badge/standard--readme-OK-green.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)
[![GitHub](https://img.shields.io/github/license/self-crafted/custom-lobby?style=flat-square&color=b2204c)](https://github.com/self-crafted/custom-lobby/blob/master/LICENSE)
[![GitHub Repo stars](https://img.shields.io/github/stars/self-crafted/custom-lobby?style=flat-square)](https://github.com/self-crafted/custom-lobby/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/self-crafted/custom-lobby?style=flat-square)](https://github.com/self-crafted/custom-lobby/network/members)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/self-crafted/custom-lobby?style=flat-square)](https://github.com/self-crafted/custom-lobby/releases/latest)
[![GitHub all releases](https://img.shields.io/github/downloads/self-crafted/custom-lobby/total?style=flat-square)](https://github.com/self-crafted/custom-lobby/releases)

custom-lobby is a small Minecraft lobby server with [Minestom](https://github.com/Minestom/Minestom) as its core.

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

Note that for compiling you need to use JDK 25.

## Usage
To run the server you need to have Java 25 runtime installed.
Use the following command to start the server.
```shell
java -jar custom-lobby-<VERSION>.jar
```
The settings are located at `./custom-lobby.json` with these default values:
```json5
{
  "SERVER_IP": "localhost",
  "SERVER_PORT": 25565,
  "MODE": "OFFLINE", // may be OFFLINE, ONLINE, BUNGEECORD or VELOCITY
  "VELOCITY_SECRET": ""
}
```
You have to restart the server for changes in there to take effect.

Note that the newest version of this server only supports 26.1.1/26.1.2 clients.
You may need to use ViaVersion on the proxy.

## API
This server itself does not add some API. But it features [Minestom's API](https://github.com/Minestom/Minestom) so you can use it from within extensions.

## Maintainers

[@offby0point5](https://github.com/offby0point5)

## Contributing

PRs accepted.

Small note: If editing the README, please conform to the [standard-readme](https://github.com/RichardLitt/standard-readme) specification.

## License

This project is licensed under the [Apache License Version 2.0](LICENSE).
