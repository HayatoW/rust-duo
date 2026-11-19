package com.rustduo.app

data class LessonIntro(val sentences: List<String>, val code: String)

object Intros {
    val byLesson = mapOf(
        0 to LessonIntro(listOf(
            "変数は値に名前を付けるもので、let で作ります。",
            "変数は最初は不変（変更不可）で、let mut と書くと後から値を変えられます。",
            "文の終わりにはセミコロン（;）を付け、println! で値を画面に表示します。"
        ), "let mut x = 5;\nx = 6;\nprintln!(\"{}\", x); // 6 を表示"),
        1 to LessonIntro(listOf(
            "let mut で作った変数は変更でき、x += 3 は x に 3 を足して保存する書き方です。",
            "同じ名前でもう一度 let を書いて新しい変数を作ることを、シャドーイングと呼びます。",
            "定数は変えない値の名前で、const と型（値の種類）を書き、計算で決まる値を指定します。"
        ), "let mut x = 2;\nx += 3; // 5\nlet x = x * 2; // シャドーイングで 10\nconst LIMIT: i32 = 10;"),
        2 to LessonIntro(listOf(
            "型は値の種類で、bool は真偽値 true（はい）と false（いいえ）を表します。",
            "i32 は負の数も扱える 32 ビット整数、u32 は負の数を扱わない 32 ビット整数です。",
            "char は 'あ' のような Unicode の 1 文字を表し、* は掛け算です。"
        ), "let n: i32 = 3;\nlet ok: bool = true;\nlet letter: char = 'あ';\nprintln!(\"{}\", n * 2); // 6"),
        3 to LessonIntro(listOf(
            "値の持ち主を所有者といい、各値の所有者は同時に 1 つです。",
            "スコープは名前を使える範囲で、{ } の内側に作った変数は外側の同名の変数とは別です。",
            "所有者がスコープを抜けると値は破棄され、後片付けの drop が呼ばれます。",
            "String はヒープという保存場所にある文字列を所有する型で、String::new() は空の文字列を作ります。"
        ), "let x = 4;\n{\n    let x = 9;\n    let s = String::new();\n} // s の文字列を後片付け\nprintln!(\"{}\", x); // 4"),
        4 to LessonIntro(listOf(
            "String::from(\"Rust\") は文字列を所有する String の値を作ります。",
            "String を別の変数へ代入すると所有権（持ち主であること）が移り、これをムーブと呼びます。",
            "ムーブ後の元の変数はそのまま使えず、使うとコンパイル（実行前の検査）でエラーになります。",
            "String をその値のまま関数へ渡すときも、通常は所有権が関数へ移ります。"
        ), "let s = String::from(\"Rust\");\nlet t = s; // 所有権が t へ移る\nprintln!(\"{}\", t); // s はもう使えない"),
        5 to LessonIntro(listOf(
            "i32 などの整数は Copy という性質があり、代入すると値がコピーされ、元の変数も使えます。",
            "String は Copy ではないので、独立した文字列を複製するには clone() を呼びます。",
            "clone() は元の所有権を奪わないので、複製した後も元の String を使えます。"
        ), "let a = 7;\nlet b = a; // a も使える\nlet s = String::from(\"Rust\");\nlet t = s.clone(); // s も使える"),
        6 to LessonIntro(listOf(
            "&s は s を借りて使う共有参照で、借用しても所有権は元の持ち主に残ります。",
            "共有参照は複数作れますが、通常はそこから値を変更できません。",
            "len() は文字列のバイト数（保存に使う大きさ）を返し、半角英字 abc は 3 バイトです。"
        ), "let s = String::from(\"abc\");\nlet r = &s;\nprintln!(\"{}\", r.len()); // 3"),
        7 to LessonIntro(listOf(
            "& は値を借りる参照を作り、&mut x は変更もできる可変参照を作ります。",
            "&mut T は型 T の値への可変参照という型の書き方で、同じ値に同時に使える可変参照は 1 つだけです。",
            "*r は参照先の値を読み書きする参照外しで、*r += 2 はその値に 2 を足します。"
        ), "let mut x = 1;\nlet r = &mut x;\n*r += 2;\nprintln!(\"{}\", x); // 3"),
        8 to LessonIntro(listOf(
            "スライスは並んだデータの一部を借りるもので、文字列スライスの型は &str です。",
            "&s[0..2] は先頭の 2 バイトを借り、半角英字の Rust なら Ru になります。",
            "配列 a の全体を借りるには &a[..] と書き、文字列を区切る場所は文字の途中にできません。",
            "参照は元の値より長くは生きられず、'a のようなライフタイム注釈は参照が使える期間の関係に付ける名前です。"
        ), "let s = \"Rust\";\nprintln!(\"{}\", &s[0..2]); // Ru\nlet a = [1, 2, 3];\nlet all = &a[..];"),
        9 to LessonIntro(listOf(
            "struct は関連するデータをまとめる構造体を定義し、その中の名前付きの値をフィールドと呼びます。",
            "Point { x: 8 } で値を作り、p.x のようにドットでフィールドを読みます。",
            "impl の中には、その構造体に関連する関数や、値に対して呼ぶメソッドを書きます。",
            "メソッドの &self は自分自身の値を共有借用する引数で、空の構造体は struct Marker; と書けます。"
        ), "struct Point { x: i32 }\nimpl Point {\n    fn x(&self) -> i32 { self.x }\n}\nlet p = Point { x: 8 };\nprintln!(\"{}\", p.x); // 8"),
        10 to LessonIntro(listOf(
            "enum はいくつかの形から 1 つを選ぶ列挙型を定義し、それぞれの形をバリアントと呼びます。",
            "Option は値の有無を表す列挙型で、Some(3) は値あり、None は値なし、Option<i32> は整数が入る場合の型です。",
            "is_some() は値があれば true を返します。",
            "Result は処理の成功と失敗を表す列挙型で、Ok が成功、Err が失敗です。"
        ), "enum Light { Red, Green }\nlet n: Option<i32> = None;\nlet n = Some(3);\nprintln!(\"{}\", n.is_some()); // true"),
        11 to LessonIntro(listOf(
            "match は値の形や値そのもの（パターン）に合わせて処理を分け、すべての場合を扱う必要があります。",
            "分岐の 1 行をアームと呼び、パターン => 式, と書き、_ は何にでも一致します。",
            "上から順に一致するものを選ぶので、_ は残りの場合を受け止める最後の行に置きます。",
            "1 つのパターンだけを簡潔に扱うなら if let を使えます。"
        ), "let n = 2;\nlet s = match n {\n    1 => \"一\",\n    _ => \"その他\",\n};\nif let Some(x) = Some(3) { println!(\"{}\", x); }")
    )
}
