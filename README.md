# JustMoney

[![Build Status](https://ci.codemc.io/buildStatus/icon?job=EpicEricEE/JustMoney&style=flat)](https://ci.codemc.io/job/EpicEricEE/job/JustMoney/)
[![GitHub release](https://img.shields.io/github/release/EpicEricEE/JustMoney.svg)](https://github.com/EpicEricEE/JustMoney/releases/)

JustMoney is a Bukkit economy plugin that adds just money, not any unnecessary features that you will never use.
Just upload the plugin to your server, and everything just works out of the box.

[**Get JustMoney on SpigotMC**](https://www.spigotmc.org/resources/justmoney.81941/)

## Features
- SQLite and MySQL database support
- [Vault](https://www.spigotmc.org/resources/vault.34315/) integration
- Optional multi world economy support
- Customizable currency formatting
- Permissions
- Automatic update checking


## Usage

### Commands
The `/money` command can also be accessed via `/balance` or `/bal`.

The optional `<world>` argument can only be used if `multi-world` is enabled in the config. If the argument is left out, the player's current world is used instead.

| Command | Description |
| --- | --- |
| `/money [<world>]` | Show your own balance |
| `/money <player> [<world>]` | Show the balance of another player |
| `/money send <player> <amount> [<world>]` | Send money to another player |
| `/money set <amount> [<world>]` | Set your own balance |
| `/money set <player> <amount> [<world>]` | Set the balance of another player |
| `/money give <amount> [<world>]` | Add money to your own account |
| `/money give <player> <amount> [<world>]` | Add money to the account of another player |
| `/money take <amount> [<world>]` | Withdraw money from your own account |
| `/money take <player> <amount> [<world>]` | Withdraw money from the account of another player |
| `/money help` | Show the command usage |

### Permissions
These should be self-explanatory.

- `justmoney.*`
- `justmoney.send` *(allowed by default)*
- `justmoney.view.other`
- `justmoney.set.*`
- `justmoney.set.self`
- `justmoney.set.other`

## Developer API
I highly recommend using [Vault](https://www.spigotmc.org/resources/vault.34315/) to hook into your server's economy, as it provides an API that almost all current economy plugins hook into. If for some reason, you still want to hook directly into JustMoney, it is published on CodeMC's maven repository:

*(Replace `{version}` with the respective version)*
```xml
<repository>
    <id>codemc-repo</id>
    <url>https://repo.codemc.io/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>de.epiceric.justmoney</groupId>
    <artifactId>justmoney</artifactId>
    <version>{version}</version>
    <scope>provided</scope>
</dependency>
```

```groovy
repositories {
    maven {
        url = "https://repo.codemc.io/repository/maven-public/"
    }
}

dependencies {
    compileOnly 'de.epiceric.justmoney:justmoney:{version}'
}
```

### Example usage
Any changes to an account's balance done via `BankAccount#setBalance(...)`, `BankAccount#withdraw(...)` or `BankAccount#deposit(...)` are automatically stored to the database or file system.

```java
World world = ...;
OfflinePlayer vendor = ...;
Player customer = ...;
double price = ...;

JustMoney plugin = JavaPlugin.getPlugin(JustMoney.class);
BankManager manager = plugin.getBankManager();

BankAccount vendorAccount = manager.getBankAccount(vendor);
BankAccount customerAccount = manager.getBankAccount(customer);

if (customerAccount.getBalance(world) >= price) {
    customerAccount.withdraw(price, world);
    vendorAccount.deposit(price, world);
} else {
    customer.sendMessage("You don't have enough money!");
    return;
}

// or using the server's default world and using try-catch

try {
    customerAccount.withdraw(price);
    vendorAccount.deposit(price);
} catch (NotEnoughMoneyException ex) {
    customer.sendMessage("You don't have enough money!");
    return;
}

customer.sendMessage("You now have " + customerAccount.formatBalance() + "left on your account!");
```
