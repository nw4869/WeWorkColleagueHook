package com.nightwind.wework;

import android.app.Application;
import android.content.Context;
import android.widget.BaseAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class WeWorkColleagueHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(lpparam.packageName.equals("com.tencent.wework")) {
            XposedBridge.log("Hooking 企业微信");
            hookColleaguePostListActivity(lpparam);
            hookColleagueBbsService(lpparam);
        }
    }

    private void hookColleagueBbsService(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                ClassLoader classLoader = ((Context)param.args[0]).getClassLoader();

                Class GetPostListCallBack = XposedHelpers.findClass(
                        "com.tencent.wework.foundation.logic.ColleagueBbsService.GetPostListCallBack",
                        classLoader
                );
                XposedBridge.log("Found GetPostListCallBack");

                findAndHookMethod("com.tencent.wework.foundation.logic.ColleagueBbsService", classLoader, "getPostList",
                        int.class, int.class, GetPostListCallBack,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                int limit = (int) param.args[1];
                                XposedBridge.log("Hooked (before) com.tencent.wework.foundation.logic.ColleagueBbsService.getPostList(...): old limit is " + limit + ", setting limit to 10000");
                                param.args[1] = 10000;
                            }
//                            private Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                int last_update_time = (int) methodHookParam.args[0];
//                                int limit = 100;
//                                Log.d("XposedTag", "Set getPostListReq.limit to 100");
//                                Object callback = methodHookParam.args[2];
//                                XposedHelpers.callMethod(param.thisObject, "getPostList", last_update_time, limit, callback);
//                                return null;
//                            }
                        }
                );
            }
        });
    }

    private void printColleagueBbsPost(Object colleagueBbsPost) {
        Object postCompleteInfo = XposedHelpers.getObjectField(colleagueBbsPost, "cGb");

//                        String postId = (String) XposedHelpers.callMethod(colleagueBbsPost, "aoW");
        Object bBSPostId = XposedHelpers.getObjectField(postCompleteInfo, "id");
        String postId = String.valueOf(XposedHelpers.getObjectField(bBSPostId, "postId"));

        String title = (String) XposedHelpers.callMethod(colleagueBbsPost, "getTitle");
        String content = (String) XposedHelpers.callMethod(colleagueBbsPost, "getContent");

        // String humanCreateTime = (String) XposedHelpers.callMethod(colleagueBbsPost, "aoN");
        long createTime = XposedHelpers.getIntField(postCompleteInfo, "createTime");
        String strCreateTime = String.valueOf(createTime);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String humanCreateTime = dateFormat.format(new Date(createTime * 1000));

        // user info 1
        Object userInfo = XposedHelpers.getObjectField(postCompleteInfo, "userInfo");
        Object author = String.valueOf(XposedHelpers.getLongField(userInfo, "userId"));
        // --- user info 2
        Object user = XposedHelpers.getObjectField(colleagueBbsPost, "cxU");
        String userDisplayName = user != null ? (String) XposedHelpers.callMethod(user, "getDisplayName") : "";
//        String mobilePhone = user != null ? (String) XposedHelpers.callMethod(user, "getMobilePhone"): "";

        String postInfo = "Post id=" + postId + ", title=" + title + ", content=" + content.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r") + ", createTime=" + strCreateTime + ", humanCreateTime=" + humanCreateTime + ", author=" + author +
                ", userDisplayName=" + userDisplayName;

        XposedBridge.log(postInfo);

    }

    private void hookColleaguePostListActivity(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ClassLoader classLoader = ((Context)param.args[0]).getClassLoader();
                findAndHookMethod("com.tencent.wework.colleague.controller.ColleaguePostListActivity", classLoader, "onResume", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("Hooked (before) com.tencent.wework.colleague.controller.ColleaguePostListActivity.onResume()");

                        BaseAdapter postListAdapter = (BaseAdapter) XposedHelpers.getObjectField(param.thisObject, "cDm");
                        for (int i = 0; i < postListAdapter.getCount(); i++) {
                            Object colleagueBbsPost = postListAdapter.getItem(i);
                            printColleagueBbsPost(colleagueBbsPost);
                        }

                    }
                });
            }
        });
    }


}
