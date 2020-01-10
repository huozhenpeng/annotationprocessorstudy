参考：https://www.jianshu.com/p/9594d2329392
@AutoService(Processor.class)//自动注册 注意这儿的Processor不要写成Process

注意调试步骤：
参考：https://zhuanlan.zhihu.com/p/38433630

高版本的gradle还是有问题，先用低版本的，可能是apt版本造成的，后期找原因


ButterKnife.bind(this);作用就是根据this找到具体的实现类，然后利用反射初始化实现类，在实现类中利用传递进去的this给this中的成员赋值

疑惑一：这两个注解声明的Retention不一样

