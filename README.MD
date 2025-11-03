<div align="center">
 <img src="src/main/resources/assets/minecraft/tritium/textures/logo_white.png" alt="Tritium Logo">
 <h1>Tritium-X</h1>
</div>

> 本项目已于 `2025/11/03` 开源。  
> 你要抄我的码子就好好抄，抄明白了，别他妈抄一个半成品出来恶心人  
> 你直接 rename 我的端子发出来或者倒卖你妈必死  
> 你有什么问题你可以加入 `617399255` 找我问，弱智问题别来，秒飞。  
> 方块人不好玩。

Tritium-X 是一个开源的 Minecraft 1.8.9 客户端。

## 亮点
- **快速**：本客户端使用了一个经过深度优化的 mcp919 base, 移除了
  - Twitch 直播
  - Snooper
  - 成就系统
  - Optifine 反射
  - ...更多香草狗屎
- **低内存占用**
- **插件支持**：实现了 **部分** Opai 扩展 API
- 更多内容自己看，我忘了。

# 更多注意事项
- 本客户端中的某些功能仅为 `Windows` 平台编写，如果你想跨平台运行的话请自行更改代码。
- 为了修复高鼠标回报率导致的卡顿，我编译了一份 `glfw` 的最新最热版本，放在 `src/main/resources/windows/x64/org/lwjgl/glfw/glfw.dll` 中。
- 更多狗屎我忘了。

# Opai 扩展 API 支持
| 模块                                                                | 状态                     |
|-------------------------------------------------------------------|------------------------|
| `addFriend` / `removeFriend` / `isFriend`                         | `Won't be implemented` |
| `addTarget` / `removeTarget` / `isTarget`                         | `Won't be implemented` |
| `OpenAPI.loadConfig(String)`                                      | `Won't be implemented` |
| `IRC`                                                             | `Won't be implemented` |
| `HypixelAPI`                                                      | `Won't be implemented` |
| `GameStateManager`                                                | `Won't be implemented` |
| `ShaderUtil`                                                      | `Low priority`         |
| `Notification`                                                    | `Low priority`         |
| `EventHandler.onStrafe(EventStrafe)`                              | `Not planned`          |
| `ItemUtil.fromData(String name, int count, int meta, String nbt)` | `Not implemented`      |

## Screenshots
<div align="center">
<img src="img/mainmenu.png" alt="Main Menu"/>
<p><b>Main Menu</b></p>
<img src="img/altmanager.png" alt="Alt Manager"/>
<p><b>Alt Manager</b></p>
<img src="img/clickgui.png" alt="Click Gui"/>
<p><b>Click Gui</b></p>
<img src="img/musicscreen.png" alt="Music Screen"/>
<p><b>Music Screen</b></p>
<img src="img/lyrics.png" alt="Lyrics"/>
<p><b>Lyrics</b></p>
<img src="img/respack%20preview.png" alt="Resourcepack Preview"/>
<p><b>Resourcepack Preview</b></p>
</div>