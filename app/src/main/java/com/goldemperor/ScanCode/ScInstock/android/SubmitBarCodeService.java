package com.goldemperor.ScanCode.ScInstock.android;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.goldemperor.MainActivity.define;
import com.goldemperor.ScanCode.ScInstock.LoginActivity;
import com.goldemperor.ScanCode.ScInstock.MainActivity;
import com.goldemperor.ScanCode.ScInstock.model.MessageEnum;
import com.goldemperor.ScanCode.ScInstock.model.MessageObject;
import com.goldemperor.ScanCode.ScInstock.model.UserInfo;
import com.goldemperor.ScanCode.ScInstock.model.UserLoginInfo;
import com.goldemperor.Utils.LOG;

import java.util.HashMap;
import java.util.Map;

//向webservice服务器提交条形码数据
public class SubmitBarCodeService extends Thread {
    protected static final String TAG = "MainActivity";
    private Dialog dd;
    public Handler myHandler;
    public Context myContext;
    public UserInfo myuserInfo;
    public String _DefaultSuitID = "1";//默认账套为金帝集团有限公司 即01.01账套
    private UserLoginInfo myuserLoginInfo = null;
    private String myallDataJson;

    public SubmitBarCodeService(Handler handler, Context context, UserLoginInfo loginActivity_instance, String allDataJson, Dialog dd) {
        myHandler = handler;
        myContext = context;
        myuserLoginInfo = loginActivity_instance;
        myallDataJson = allDataJson;
        this.dd = dd;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        LOG.e("提交成功11111111111111111111111111111111");
//        try {
        if (myuserLoginInfo == null)
            return;
        String params;
//            String params = GetDefaultStockParams(myuserLoginInfo.userInfo);
//            String DefaultStockID = PublicService.GetWebServiceParamsComnon(myContext, StockBarCodeService.asmxURL, "GetDefaultStockID", params);
        String DefaultStockID = myuserLoginInfo.userInfo.getDefaultStockID();
        LOG.e("--------------DefaultStockID=" + DefaultStockID);
        int DefaultID = 0;
        try {
            DefaultID = Integer.parseInt(DefaultStockID);
        } catch (Exception e) {
            Toast.makeText(myContext, "DefaultStockID没有" + DefaultStockID, Toast.LENGTH_SHORT).show();
            return;
        }

        if (DefaultID == 0) {
            Looper.prepare();
            Toast.makeText(myContext, "当前登录用户尚未配置默认仓库,请联系系统管理员", Toast.LENGTH_SHORT).show();
            dd.dismiss();
            Looper.loop();
            return;
        }

        params = GetBarCodeParams(myuserLoginInfo.userInfo);
        //设定账套下拉框 ,发送显示指令单命令 Looper.prepare();
//            String result = PublicService.GetWebServiceParamsComnon(myContext, StockBarCodeService.asmxURL, "SubmitBarCode2CollectBill", params);
        String result = PublicService.GetWebServiceParamsComnon(myContext, define.Net2+"ErpForAndroidStockServer.asmx", "SubmitBarCodeBarCode2PrdInStockCollectBillByEmpCode", params);


        //  String result="{'Result':'success','StockBillNO':''}";
        result = URLCode.toURLDecoded(result);
        result = "{'result':" + result + "}";
        String Sendresult = result;
        LOG.e("提交成功：" + result);
        result = PublicService.parseStockResultJson(myuserLoginInfo.userInfo, result);
        //if (result.equals("success")) {
        Looper.prepare();
        //new Dialog(myContext,myHandler).ClearBarCodeDataDialog("提示", "已成功生成单据编号："+myloginActivity_instance.userLoginInfo.userInfo.getStockBillNO());
        //Toast.makeText(myContext, "成功生成单据编号："+myloginActivity_instance.userLoginInfo.userInfo.getStockBillNO(), Toast.LENGTH_SHORT).show();
        //删除本地所有扫描的条形码数据
        Message message = new Message();
        MessageObject myMessageObject = new MessageObject();
        myMessageObject.Content = Sendresult;//myloginActivity_instance.userLoginInfo.userInfo.getStockBillNO();
        myMessageObject.context = myContext;
        message.what = MessageEnum.SubmitClearData;
        message.obj = myMessageObject;
        myHandler.sendMessage(message);
        Looper.loop();
           /* }
            else
            {
                Looper.prepare();
                new Dialog(myContext,myHandler).ClearBarCodeDataDialog("错误", myloginActivity_instance.userLoginInfo.userInfo.getStockBillNO());
                Looper.loop();
            }*/
//        }catch (Exception ex) {
//            //Toast.makeText(myContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
//            LOG.e(ex.getMessage());
//            Looper.prepare();
//            new Dialog(myContext).ShowAlertDialog("异常",ex.getMessage());
//            Looper.loop();
//        }
    }
    //获得参数

    public static String GetDefaultStockParams(UserInfo userInfo) {
        Map<String, String> ParamsMap = new HashMap<String, String>();
        String params = "";
        ParamsMap.put("OrganizeID", userInfo.getOrganizationID());
        ParamsMap.put("UserID", userInfo.getUserID());
        int i = 0;
        for (Map.Entry<String, String> entry : ParamsMap.entrySet()) {
            if (i == 0)
                params = entry.getKey() + "=" + entry.getValue();
            else {
                params += "&" + entry.getKey() + "=" + entry.getValue();
            }
            i++;
        }
        LOG.e("params :" + params);
        return params;
    }

    //获得参数
    public String GetBarCodeParams(UserInfo userInfo) {
        Map<String, String> ParamsMap = new HashMap<String, String>();
        String params = "";
        ParamsMap.put("EmpCode", MainActivity.userNumber);
        ParamsMap.put("UserID", userInfo.getUserID());
        ParamsMap.put("Red", userInfo.getRed());
        ParamsMap.put("DefaultStockID", userInfo.getDefaultStockID());
        ParamsMap.put("BillTypeID", userInfo.getBillTypeID());
        ParamsMap.put("OrganizeID", userInfo.getOrganizationID());
        ParamsMap.put("barcodeJson", URLCode.toURLEncoded(myallDataJson));

        int i = 0;
        for (Map.Entry<String, String> entry : ParamsMap.entrySet()) {
            if (i == 0)
                params = entry.getKey() + "=" + entry.getValue();
            else {
                params += "&" + entry.getKey() + "=" + entry.getValue();
            }
            i++;
        }
        LOG.e("生产扫码入库提交params :" + params);
        return params;
    }

}
