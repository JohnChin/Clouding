package com.example.mypc.cloudstorage.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.bean.UserBean;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

import static com.example.mypc.cloudstorage.R.id.name_account;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton, registerButton;
    private EditText editAccount, editPassword;
    private Context context;
    private UserBean currentUser;
    private ImageView closeApp;
    private TextView forgetPassword, noPasswordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        context = this;

        currentUser = BmobUser.getCurrentUser(UserBean.class);

        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        initView();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editAccount.getText().length() == 0)
                    Toast.makeText(context, "请输入用户名", Toast.LENGTH_LONG).show();
                else if (editPassword.getText().length() == 0)
                    Toast.makeText(context, "请输入密码", Toast.LENGTH_LONG).show();
                else {
                    BmobUser user = new BmobUser();
                    user.setUsername(editAccount.getText().toString());
                    user.setPassword(editPassword.getText().toString());

                    user.login(new SaveListener<BmobUser>() {
                        @Override
                        public void done(BmobUser bmobUser, BmobException e) {
                            if (e == null) {
                                Toast.makeText(context, "登录成功", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(context, "用户名或者密码错误", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Register();
            }
        });

        closeApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        noPasswordLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noPasswordLogin();
            }
        });
    }

    private void initView() {
        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);
        editAccount = (EditText) findViewById(R.id.account);
        editPassword = (EditText) findViewById(R.id.password);
        closeApp = (ImageView) findViewById(R.id.close_app);
        forgetPassword = (TextView) findViewById(R.id.forget_password);
        noPasswordLogin = (TextView) findViewById(R.id.no_password_login);
    }

    public void Register() {
        LayoutInflater inflater = getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_register, (ViewGroup) findViewById(R.id.linear_register));
        AlertDialog alertDialogRegister = new AlertDialog.Builder(context).setView(dialog).create();
        alertDialogRegister.setCanceledOnTouchOutside(false);
        Button confirmButton = (Button) dialog.findViewById(R.id.confirm_button);
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        Button getSmsButton = (Button) dialog.findViewById(R.id.get_sms_code);

        EditText editSetAccount = (EditText) dialog.findViewById(R.id.register_account);
        EditText editSetPassword = (EditText) dialog.findViewById(R.id.register_password);
        EditText editConfirmSetPassword = (EditText) dialog.findViewById(R.id.register_check_password);
        EditText editSetPhone = (EditText) dialog.findViewById(R.id.register_phone);
        EditText editSetSmsCode = (EditText) dialog.findViewById(R.id.register_sms_code);

        getSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editSetPassword.getText().length() == 0)
                    Toast.makeText(context, "请输入手机号", Toast.LENGTH_LONG).show();
                else
                {
                    BmobSMS.requestSMSCode(editSetPhone.getText().toString(), "注册验证码", new QueryListener<Integer>() {
                        @Override
                        public void done(Integer smsId, BmobException ex) {
                            if (ex == null) {//验证码发送成功
                                Toast.makeText(context, "验证码发送成功", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    getSmsButton.setClickable(false);
                    getSmsButton.setTextColor(ContextCompat.getColor(context,R.color.dividerColor));
                }

            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editSetAccount.getText().length() == 0)
                    Toast.makeText(context, "请输入用户名", Toast.LENGTH_LONG).show();
                else if (editSetPassword.getText().length() == 0)
                    Toast.makeText(context, "请输入密码", Toast.LENGTH_LONG).show();
                else if (editConfirmSetPassword.getText().length() == 0)
                    Toast.makeText(context, "请确认密码", Toast.LENGTH_LONG).show();
                else if (!editSetPassword.getText().toString().equals(editConfirmSetPassword.getText().toString()))
                    Toast.makeText(context, "两次输入的密码不相同", Toast.LENGTH_LONG).show();
                else {
                    UserBean user = new UserBean();
                    user.setUsername(editSetAccount.getText().toString());
                    user.setPassword(editSetPassword.getText().toString());
                    user.setMobilePhoneNumber(editSetPhone.getText().toString());
                    user.signOrLogin(editSetSmsCode.getText().toString(), new SaveListener<UserBean>() {
                        @Override
                        public void done(UserBean userBean, BmobException e) {
                            if (e == null) {
                                Toast.makeText(context, "注册成功", Toast.LENGTH_LONG).show();
                                alertDialogRegister.dismiss();
                            } else {
                                Toast.makeText(context, "注册失败", Toast.LENGTH_LONG).show();
                                alertDialogRegister.dismiss();
                            }
                        }
                    });
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogRegister.dismiss();
            }
        });
        alertDialogRegister.setTitle("注册账号");
        alertDialogRegister.show();
    }


    private void noPasswordLogin() {
        LayoutInflater inflater = getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_no_password_login, (ViewGroup) findViewById(R.id.linear_no_password_login));
        AlertDialog alertDialognoPassword = new AlertDialog.Builder(context).setView(dialog).create();
        alertDialognoPassword.setCanceledOnTouchOutside(false);

        EditText noPasswordPhone = (EditText) dialog.findViewById(R.id.no_password_login_phone);
        EditText noPasswordSmsCode = (EditText) dialog.findViewById(R.id.no_password_login_sms_code);

        Button noPasswordGetSmsCode = (Button) dialog.findViewById(R.id.no_password_get_sms_code);

        ImageView noPasswordConfirm = (ImageView) dialog.findViewById(R.id.confirm_no_password_login);
        ImageView noPasswordCancel = (ImageView) dialog.findViewById(R.id.cancel_no_password_login);

        noPasswordGetSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noPasswordPhone.getText().length() == 0)
                    Toast.makeText(context, "请输入电话号码", Toast.LENGTH_LONG).show();
                else
                {
                    BmobSMS.requestSMSCode(noPasswordPhone.getText().toString(), "登录验证码", new QueryListener<Integer>() {
                        @Override
                        public void done(Integer smsId, BmobException ex) {
                            if (ex == null) {//验证码发送成功
                                Toast.makeText(context, "验证码短信已发送，请注意查收", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                noPasswordGetSmsCode.setClickable(false);
                noPasswordGetSmsCode.setTextColor(ContextCompat.getColor(context,R.color.dividerColor));
            }
        });

        noPasswordConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobUser.loginBySMSCode(noPasswordPhone.getText().toString(), noPasswordSmsCode.getText().toString(), new LogInListener<BmobUser>() {
                    @Override
                    public void done(BmobUser user, BmobException e) {
                        if (user!= null) {
                            Toast.makeText(context, "登录成功", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                            alertDialognoPassword.dismiss();
                            finish();
                        }
                        else{
                            Toast.makeText(context, "验证码错误", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        noPasswordCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialognoPassword.dismiss();
            }
        });
        alertDialognoPassword.show();
    }

    private void forgetPassword() {

    }
}
