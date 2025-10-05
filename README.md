# Myau

French dogshit 1.8.9 Forge Client Source code eclipse ready (run/build able)

![1](/images/image.png)

Deobfuscated by [DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1) in less than 1 hours with ASM Model Context Protocol.

<img src="https://github.com/deepseek-ai/DeepSeek-V2/blob/main/figures/logo.svg?raw=true" width="60%" alt="DeepSeek-R1" />

## Description

Some French gay motherfuckers coded this haked client with bypasses that are always behind mainstream clients updates take at least a month and are usually outdated skidded trash.

The client is almost 2 years old, and they still haven't even implemented a fucking clickgui, the only way to configure this thing is by typing in retarded commands and hope for your changes to take tiny effect.

I wonder who's actually using this piece of shit.

## Building

```bash
gradlew build
```

## Note & FAQ

### Bypass broken

To be honest, I haven't played Hypixel since 2022, so I didn't even realize bypasses were broken because of AI hallucinations.

Thanks to everyone who pointed that out — I fixed the autoblock and other blink-based bypasses in the latest commit.

### Why Delay Velocity isn't working

You need to set both `horizontal` and `vertical` to `100` in the velocity settings for Delay & Jump velocity to work.

Paid Myau has the same issue.

### Is this the real source?

Strictly speaking, no — this is decompiled code from Myau-250910.

### RAT?

If you think this source might contain a RAT, don’t just assume — provide evidence.