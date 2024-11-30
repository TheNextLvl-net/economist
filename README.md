# Economist

**Economist** is a powerful economy management plugin for game servers.
It supports both global and per-world economies,
offering a comprehensive and realistic banking system with no limits on user wealth.
The plugin is built on a robust,
thread-safe API, ensuring stability and reliability in high-traffic environments.

> [!IMPORTANT]
> Bank support does not work yet and will be introduced properly in v1

## Pictures

![baltop](https://github.com/user-attachments/assets/9cc270a2-9292-4e99-85bc-093139c065b3)
![transactions](https://github.com/user-attachments/assets/cd63b2c9-e35d-4b55-b280-e64f4cc12010)
![multi-world-accounts](https://github.com/user-attachments/assets/7cf8fb8d-12d4-4213-ba39-4fe3f6cec057)




## Features

- **Global and Per-World Economy**: Manage economies globally or separately for each world.
- **Banking System**:
    - Manage banks with the `/bank` command.
    - Banks can have multiple members, with one owner.
    - Each user can own only one bank but may be a member of several.
    - Banks are uniquely identified by their names.
- **User Account Management**:
    - Use the `/Account` command to manage user accounts.
- **Currency Management**:
    - `/economy`: Give or take money from users.
    - `/Pay`: Players can transfer money to other players.
- **Richest Players Ranking**:
    - `/baltop`: View a paginated, sorted list of the richest players.
- **Customizable and Localized Messages**: All messages can be customized and localized.
- **Thread-Safe API**: Ensures stability in high-traffic environments.
- **Number Formatting Options**: Supports large numbers with scientific notation or suffixes up to 1e306.
- **Unlimited Wealth**: No hard limits on the amount of money a user can hold, making it ideal for realistic economies.

## Commands

- **/economy**: Manage the economy by giving or taking money from users.
- **/baltop**: Get a deeper insight into the economy by listing it paginated and sorted by the richest players.
- **/bank**: Manage everything bank-related, including creating, joining, and managing banks.
- **/Account**: Manage user accounts.
- **/Pay**: Pay another player money.

## Installation

1. Download the latest version of the Economist plugin.
2. Place the `.jar` file in your server's `plugins` directory.
3. Start or restart your server to load the plugin.
4. Configure the plugin as needed in the `Economist` configuration files.

## Configuration

Economist is highly configurable.
Every message can be changed, and full localization is supported.
Check the configuration files located in the `Economist` directory for all available settings.

## Permissions

- `economist.admin`: Access to all admin commands like `/economy`.
- `economist.baltop`: Permission to view the `/baltop` leaderboard.
- `economist.bank`: Permission to create and manage banks.
- `economist.account`: Permission to manage user accounts.
- `economist.pay`: Permission to use the `/pay` command.
