package com.example.calculator.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calculator.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    /**数字用の蓄積文字列*/
    private String num_str = "";
    /**最新の蓄積文字列のみを格納する*/
    private String last_num_str = "";
    /**クリック時の演算子格納用*/
    private char ope_chr = ' ';
    /**四則演算子を保存するリスト*/
    private List<Character> ope_list = new ArrayList<>();
    /**数字を保存するリスト*/
    private List<BigDecimal> num_list = new ArrayList<>();
    /**=で結果が出たときから次の数字入力があるまではtrueの変数*/
    private boolean exist_result = false;
    /**0で割ろうとするとtrueになる*/
    private boolean zero_sign = false;

    /**数字ボタンをクリックした際の挙動を記述
     * @param view ボタンのインスタンスをビューとして受け取る*/
    public void clickNumber(View view){
        //受け取ったボタンインスタンスの数文字をchar型で受け取る
        char num_text = ((TextView)view).getText().charAt(0);

        //結果が出た直後の入力ならば
        if(exist_result){
            //-----------=の結果は消える------------
            ((TextView)findViewById(R.id.textView)).setText("");
            num_str = "";
            exist_result = false;
        }
        //数値の入力を15桁に制限
        if(num_str.length() < 15) {
            //蓄積数値が0だけのとき
            if (num_str.length() == 1 && num_str.charAt(0) == '0') {
                //0を消去し、新しい数値で置き換える
                num_str = "";
                //蓄積が0のみの数値を塗り替えるため、0割りが無くなる
                zero_sign = false;
                String text = ((TextView) findViewById(R.id.textView)).getText().toString();
                if (text.length() == 1)
                    ((TextView) findViewById(R.id.textView)).setText("");
                else
                    ((TextView) findViewById(R.id.textView)).setText(text.substring(0, text.length() - 1));
            }
            //数字なので蓄積数値文字列に追加
            num_str += num_text;
            //テキストビューへ追加
            insertText(num_text);
        }
        //galaxy仕様：結果随時更新（演算子が１つでも表示されているなら実行）
        if(!(ope_list.isEmpty())){
            addList(num_str);
            //一時結果を取得
            String sub_result = calculate();
            //ここでは入れた要素をすぐに削除する（しないと=のときに狂う）
            num_list.remove(num_list.size() - 1);
            //不正numをリストから削除したので、zero_signをfalseにする
            zero_sign = false;
            //不正な結果の場合は何も表示しない
            if(!(sub_result.equals("no_result")))
                ((TextView) findViewById(R.id.textView_ans)).setText(sub_result);
        }
    }
    /**四則演算子ボタンをクリックした際の挙動を記述
     * @param view ボタンのインスタンスをビューとして受け取る*/
    public void clickOperator(View view){
        //受け取ったボタンインスタンスの演算子文字をchar型で受け取る
        ope_chr = ((TextView)view).getText().charAt(0);

        //ビューの最終文字がoperatorではなく、初期状態からの入力でないとき
        if(!isOperator() && !(num_str.equals(""))) {
            insertText(ope_chr);
            addList(num_str, ope_chr);
        }//四則演算子を連続で入力したとき
        else if(isOperator()){
            replaceText(ope_chr);//上のinsertTextで追加された文字を新たなope_textで入れ替え
            ope_list.remove(ope_list.size()-1);//size()は要素数,remove()はindex指定
            ope_list.add(ope_chr);
        }
        //++処理のために第二項を保持
        last_num_str = num_str;
        num_str = "";
        exist_result = false;
        //galaxy電卓仕様：演算子ボタンを押すとサブビュー表示がリセット
        ((TextView)findViewById(R.id.textView_ans)).setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //theme指定
        setTheme(R.style.AppTheme_NoTitleBar);
        //xmlをアクティビティに適用する
        setContentView(R.layout.activity_main);

        /*「.」ボタンを押したとき*/
        findViewById(R.id.button_point).setOnClickListener(v -> {
            TextView textView = findViewById(R.id.textView);
            //結果が出た直後の入力ならば
            if(exist_result){
                //-----------=の結果は消える------------
                textView.setText("");
                num_str = "";
                exist_result = false;
            }
            //「.」が連続入力ではないとき
            if(textView.getText().toString().isEmpty()||textView.getText().toString().charAt(textView.getText().toString().length()-1) != '.') {
                //数字入力がない場合であっても
                if (num_str.equals("")) {
                    insertText('0');
                    num_str += '0';
                }
                //ビューに文字を追加する
                insertText('.');
                num_str += '.';
            }
        });
        /*「AC」（all clear）ボタンを押したとき、各種初期状態に戻す*/
        findViewById(R.id.button_ac).setOnClickListener(v -> {
            ((TextView)findViewById(R.id.textView)).setText("");
            ((TextView)findViewById(R.id.textView_ans)).setText("");
            num_str = "";
            exist_result = false;
            ope_chr = ' ';
            zero_sign = false;
            allClear();
        });
        /*「←」1文字削除*/
        findViewById(R.id.button_c).setOnClickListener(v -> {
            //テキストビューの置き換え
            String str = ((TextView)findViewById(R.id.textView)).getText().toString();
            //初期状態ではなく、何か表示しているとき
            if(!(str.equals("")) && str.length() > 0) {
                //消したい文字が演算子であれば
                if(isOperator()){
                    /*第一項数字が押され、演算子が押されていない状況
                    （num_strが第一項であるとき）を再現*/
                    num_str = num_list.get(num_list.size()-1).toString();
                    //ope_listの最新の文字を消す
                    ope_list.remove(ope_list.size()-1);
                    //num_listの最新の文字を消す
                    num_list.remove(num_list.size()-1);

                    //ope_listが空（まだ1項目の入力後）のとき
                    if(ope_list.isEmpty())
                        ope_chr = ' ';
                    else
                        ope_chr = ope_list.get(ope_list.size()-1);
                }else{
                    //蓄積数値文字列から最新の１文字を消した文字列を取得し代入
                    num_str = num_str.substring(0,num_str.length()-1);
                    ((TextView)findViewById(R.id.textView_ans)).setText("");
                }
                //現在のビューから最後の1文字を減らしたビュー文字列を取得
                str = str.substring(0, str.length() - 1);
                exist_result = false;
            }
            ((TextView)findViewById(R.id.textView)).setText(str);
        });
        /*「＝」結果を表示*/
        findViewById(R.id.button_ans).setOnClickListener(v -> {
            //受け取ったボタンインスタンスの演算子文字をchar型で受け取る
            //ope_chr = ((TextView)v).getText().charAt(0);

            //定期的に第二項を保持
            if(!exist_result) {
                //++処理のために
                last_num_str = num_str;
            }
            //ビューの最終文字がoperatorではなく、初期状態からの入力でないとき
            if (!isOperator() && !(num_str.equals("")) && ope_chr != ' ') {
                //とりあえずリストへ追加
                addList(num_str);

                //=が連続して入力されたとき
                if (exist_result) {//例えば、90+3===...としていくと、93,96,99,...となる
                    //第二項と第二項が処理される演算子をリストへ
                    addList(last_num_str, ope_chr);
                }
                //計算結果を文字列で受け取る
                String result = calculate();
                //不正な結果でないとき
                if (!(result.equals("no_result"))) {
                    //結果をテキストビューに適用
                    ((TextView) findViewById(R.id.textView)).setText(result);
                    //各リストを空にする
                    allClear();
                    //この結果を入力（数字のクリック）とする
                    num_str = result;
                    //結果が出たことを知らせる
                    exist_result = true;
                } else {
                    //ここで文字警告
                }

            }//四則演算子を連続で入力したとき（今回は+=や*=）
            else if (isOperator()) {
                //galaxyの電卓では、警告文が数秒だけ表示される
                //replaceText(ope_text);上のinsertTextで追加された文字を新たなope_textで入れ替え
                //ope_list.remove(ope_list.size()-1);//size()は要素数,remove()はindex指定
                Toast toast = Toast.makeText(this,"無効な入力です",Toast.LENGTH_LONG);
                toast.show();
            }
            ((TextView) findViewById(R.id.textView_ans)).setText("");
        });

    }
    /**現在のテキストビューの最後の文字が演算子文字かどうかを判定するメソッド
     * @return 四則演算子の文字が最後の場合にtrue,そうでないならばfalse*/
    private boolean isOperator(){
        //テキストビュー取得
        TextView textView = findViewById(R.id.textView);
        //テキストビューの文字列を取得
        String str = textView.getText().toString();

        //テキストビューの文字列の最後の文字を取得
        if(str.length() > 0) {
            char str_end = str.charAt(str.length() - 1);
            return str_end == '+' || str_end == '-' || str_end == '×' || str_end == '÷';
        }
        return false;
    }
    /**現在のテキストビューの値に、新たな文字を結合するメソッド
     * @param text 結合する文字*/
    private void insertText(char text){
        TextView textView = findViewById(R.id.textView);
        String combine = textView.getText().toString() + text;
        textView.setText(combine);
    }
    /**表示されている演算子を置き換えするためのメソッド
     * @param text 新たな四則演算子*/
    private void replaceText(char text){
        //テキストビューを取得
        TextView textView = findViewById(R.id.textView);
        //テキストビューの最終演算文字を除き、新たな演算文字で置き換えた文字列を取得
        String replace = textView.getText().toString().substring(0,textView.getText().toString().length()-1) + text;
        //上の文字列replaceをテキストビューの値と置き換える
        textView.setText(replace);
    }
    /**蓄積数値文字列と演算子文字をそれぞれリストに格納するためのメソッド
     * @param num 格納する数値文字列
     * @param ope 格納する演算子文字*/
    private void addList(String num,Character ope){
        num_list.add(new BigDecimal(num));
        ope_list.add(ope);
    }
    /**蓄積数値文字列をリストに格納するためのメソッド
     * @param num 格納する数値文字列*/
    private void addList(String num){
        num_list.add(new BigDecimal(num));
    }
    /**num_listとope_listを空にするためのメソッド*/
    private void allClear(){
        num_list.clear();
        ope_list.clear();
    }
    /**式に0で割る処理が含まれているかを返すメソッド
     * @return 0割りが存在するならばtrue,そうでないならばfalse*/
    private boolean inZero(){
        int i = 0;
        while(i < ope_list.size()){
            if(ope_list.get(i).equals('÷') && num_list.get(i+1).equals(BigDecimal.ZERO)) {
                num_list.remove(i+1);
                return true;
            }
        }
        return false;
    }
    /**計算メソッド
     * @return 構文解析による計算結果*/
    private String calculate(){
        int i = 0;
        //リストを別リストへコピー
        List<BigDecimal> sub_num_list = new ArrayList<>();
        for(int x = 0;x <= num_list.size()-1;x++)
            sub_num_list.add(x,num_list.get(x));
        List<Character> sub_ope_list = new ArrayList<>();
        for(int y = 0;y <= ope_list.size()-1;y++)
            sub_ope_list.add(y,ope_list.get(y));

        while(i < sub_ope_list.size() && !zero_sign){
            if(sub_ope_list.get(i).equals('×') || sub_ope_list.get(i).equals('÷')){
                if(sub_ope_list.get(i).equals('÷') && sub_num_list.get(i+1).equals(BigDecimal.ZERO)){
                    zero_sign = true;
                }else {
                    //opeが×のときと÷のときのresult結果を分岐
                    BigDecimal result = sub_ope_list.get(i).equals('×') ? sub_num_list.get(i).multiply(sub_num_list.get(i + 1)) : sub_num_list.get(i).divide(sub_num_list.get(i + 1), 10, BigDecimal.ROUND_DOWN);
                    sub_num_list.set(i, result);
                    sub_num_list.remove(i + 1);
                    sub_ope_list.remove(i);
                    i--;
                }
            }
            else if(sub_ope_list.get(i).equals('-')){
                sub_ope_list.set(i,'+');
                sub_num_list.set(i+1,sub_num_list.get(i+1).negate());
            }
            i++;
        }
        //0で割る処理が無かった場合
        if(!zero_sign) {
            BigDecimal res = new BigDecimal("0");
            for (BigDecimal num : sub_num_list)
                res = res.add(num);
            return res.toString();
        }
        return "no_result";
    }
}