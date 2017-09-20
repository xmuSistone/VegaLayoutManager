# VegaLayoutManager
a customized RecyclerView.LayoutManager which fade and shrink the head itemView when scrolling.

### 效果
![preview](https://camo.githubusercontent.com/bb984a34320d944ccf561857995c90629f5037a0/68747470733a2f2f662e666c6f636b75736572636f6e74656e74322e636f6d2f646334323539613135303438303136333139393038353836)

dribbble设计：[链接](https://dribbble.com/shots/3793079-iPhone-8-iOS-11)<br>
Ios实现：[VegaScroll](https://github.com/AppliKeySolutions/VegaScroll)

### 代码实现
方案比较简单易懂，使用自定义的LayoutManger，内置SnapHelper。<br>
由于想要在任意时刻都能snap到第一个子View，所以在LayoutManager中用了比较讨巧的方法去设定scroll的最大值。

### 使用方法
```java
recyclerView.setLayoutManager(new VegaLayoutManager());
```

### demo下载
[点击下载](https://github.com/xmuSistone/VegaLayoutManager/blob/master/app-debug.apk?raw=true)
