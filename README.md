# FlixAH 🏷️
> **Clean, High-Performance Auction House Plugin for PaperMC 1.20 – 1.21+**  
> Developed by **FlixStudios** (Tejas)

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20%20--%201.21+-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

---

## ✨ Features
* **Zero-Lag Architecture:** Built with clean OOP principles and asynchronous expiration routines.
* **Vault Economy Integration:** Direct compatibility with EssentialsX, CoinsEngine, and any Vault-backed economy.
* **Anti-Dupe Escrow:** Validates inventory items and clears hand slots atomically before market registration.
* **Smart Mailbox:** Unclaimed or expired auctions go into an offline `/ah mail` collection box if the player's inventory is full.
* **Paginated GUI:** Smooth multi-page chest navigation with live countdown timers.

---

## 📌 Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/ah` | `flixah.use` | Opens the main Auction House menu |
| `/ah sell <price>` | `flixah.sell` | Lists the item currently held in main hand |
| `/ah mail` | `flixah.use` | Claims expired or won items from your mailbox |
| `/ah reload` | `flixah.admin` | Reloads `config.yml` and `messages.yml` |

---

## 🛠️ Building From Source

```bash
# Clone the repository
git clone https://github.com/tejasjhain-sudo/FlixAH.git
cd FlixAH

# Compile JAR with Maven
mvn clean package
```

The compiled JAR will be in `target/FlixAH-1.0.0.jar`.

---

## 📄 License
Licensed under the [MIT License](LICENSE). Built for the Minecraft community by **FlixStudios**.
