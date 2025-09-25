# INCLOVE-BACKEND

## How to Contribute?

1. Fork this repository
2. Add code
3. Submit a PR request (Please specify what the submitted code does, such as "Fixed the login function")
4. Code review
5. Merge into the main branch

聊天室表结构：

参考文章：https://learn.lianglianglee.com/%e4%b8%93%e6%a0%8f/%e5%8d%b3%e6%97%b6%e6%b6%88%e6%81%af%e6%8a%80%e6%9c%af%e5%89%96%e6%9e%90%e4%b8%8e%e5%ae%9e%e6%88%98/02%20%e6%b6%88%e6%81%af%e6%94%b6%e5%8f%91%e6%9e%b6%e6%9e%84%ef%bc%9a%e4%b8%ba%e4%bd%a0%e7%9a%84App%ef%bc%8c%e5%8a%a0%e4%b8%8a%e5%ae%9e%e6%97%b6%e9%80%9a%e4%bf%a1%e5%8a%9f%e8%83%bd.md

1.在聊天记录方面，存在两个表

    1.聊天记录表：存储所有消息

    2.聊天索引表：存储所有消息，并且一条消息从发信人和收信人两个方向记录，
    通过消息id进行去区分，因为可能存在a删除了聊天记录，但是b需要正常显示这种需求

2.在联系人方面，存在一个表

    1.联系人列表：一个聊天关系存两个记录，一个类似 张三id，李四id，最新消息id
    另一个类似 李四id，张三id，最新消息id
    
    这个表取代了旧版本里面的Conversation表的设计，为什么呢？
    个人认为原因主要是：如果已经有了对话表，那联系人表还要不要呢？从业务逻辑上来说，联系人表是必须的，符合业务语义，那么对话表的内容和联系人表就没有区别，那还不如直接去掉联系人表
