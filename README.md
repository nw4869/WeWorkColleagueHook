# WeWorkColleagueHook

利用Xposed框架导出企业微信同事吧帖子：

0. Hook Application.attach 由于企业微信使用了MultiDex，有些类在后面才会加载进来，在Application.attach结束后再进行后续 Hook
1. Hook ColleagueBbsService.getPostList（从远程拉取帖子）把每次获取帖子数的参数limit从20改成10000，一次性获取全部帖子
2. Hook ColleaguePostListActivity.onResume（同事吧帖子列表Activity），获取帖子集合Filed，逆向分析实体构成，遍历打印帖子实体

详见：[com.nightwind.wework.WeWorkColleagueHook](https://github.com/nw4869/WeWorkColleagueHook/blob/master/app/src/main/java/com/nightwind/wework/WeWorkColleagueHook.java)
