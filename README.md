# VegaLayoutManager
a customized LayoutManager - fade and shrink the head itemView when scrolling.

### 实现效果
<img src="capture.gif" width="373" height="532"/><img style width="2px" /><img src="capture2.png" width="480" height="532"/>
dribbble设计：[链接](https://dribbble.com/shots/3793079-iPhone-8-iOS-11)<br>
IOS实现：[VegaScroll](https://github.com/AppliKeySolutions/VegaScroll)

### 代码思路
RecyclerView最顶部的itemView，会随着手指滑动实现收缩隐藏与放大显示，并伴随recycler的回收与复用。<br><br>
代码比较简单粗暴，使用自定义的LayoutManger，内置SnapHelper。<br>
由于想要在任意时刻都能snap到第一个子View，所以在LayoutManager中用了比较讨巧的方法去设定scroll的最大值。

### 使用方法
一行代码搞定：
```java
recyclerView.setLayoutManager(new VegaLayoutManager());
```

### demo下载
[点击下载](https://github.com/xmuSistone/VegaLayoutManager/blob/master/app-debug.apk?raw=true)
