package com.rustduo.app

private fun q(prompt: String, answer: String, a: String, b: String, c: String, why: String) =
    Question(prompt, listOf(answer, a, b, c), answer, why)
private fun code(source: String, answer: String, a: String, b: String, c: String, why: String) =
    Question("このコードの出力は？", listOf(answer, a, b, c), answer, why, Kind.CODE, source)
private fun tile(prompt: String, answer: String, why: String, vararg words: String) =
    Question(prompt, words.toList(), answer, why, Kind.TILES)

object Curriculum {
    val units = listOf(
        UnitContent("基本", "変数・可変性・型", listOf(
            Lesson(0, "変数との出会い", listOf(
                q("変数を宣言するキーワードは？", "let", "var", "val", "define", "Rust では let で値に名前を付け、変数を作ります。"),
                code("fn main() {\n    let x = 5;\n    println!(\"{}\", x);\n}", "5", "x", "0", "エラー", "x に入れた値は 5 なので、println! で 5 が表示されます。"),
                tile("不変な変数 n に 8 を代入しよう", "let n = 8;", "let で変数 n を作り、= で 8 を入れ、; で文を終えます。mut がないので後から値を変えられません。", "8;", "=", "n", "let"),
                q("let x = 5; の x は？", "変更不可", "常に可変", "文字列", "関数", "mut を付けずに作った変数は、後から値を変えられません。"),
                q("文の終わりに使う記号は？", ";", ":", ",", "#", "多くの文はセミコロンで終わります。")
            )),
            Lesson(1, "可変性", listOf(
                q("変数を変更可能にするのは？", "mut", "const", "static", "use", "let に mut を付けると、後から値を変えられる変数になります。"),
                code("fn main() {\n    let mut x = 2;\n    x += 3;\n    println!(\"{}\", x);\n}", "5", "2", "3", "エラー", "可変な x に 3 を足すので 5 です。"),
                tile("可変な変数 x に 5 を代入しよう", "let mut x = 5;", "let で変数 x を作り、mut で変更できるようにして、= で 5 を入れ、; で文を終えます。", "let", "mut", "x", "=", "5;"),
                q("同名の let で再束縛する機能は？", "シャドーイング", "借用", "移動", "継承", "let を使って同じ名前を再利用できます。"),
                q("定数を宣言するのは？", "const", "mut", "impl", "mod", "const では値の種類（型）を書き、実行前に計算できる値を指定します。")
            )),
            Lesson(2, "型の基礎", listOf(
                q("真偽値の型は？", "bool", "boolean", "bit", "truth", "bool は true と false を表します。"),
                code("fn main() {\n    let n: i32 = 3;\n    println!(\"{}\", n * 2);\n}", "6", "3", "32", "エラー", "i32 は負の数も扱える 32 ビット整数の型です。3 に 2 を掛けるので 6 です。"),
                tile("真偽値を宣言しよう", "let ok = true;", "let で変数 ok を作り、= で「はい」を表す true を入れ、; で文を終えます。", "true;", "let", "=", "ok"),
                q("1 文字を表す型は？", "char", "str", "String", "byte", "char は 'あ' のような文字を 1 つ表します。絵文字など、見た目は 1 文字でも複数の char が必要なものがあります。"),
                q("符号なし 32 ビット整数は？", "u32", "i32", "f32", "bool", "u32 の u は負の数を扱わない整数、32 は保存に使うビット数を表します。")
            ))
        )),
        UnitContent("所有権", "値の持ち主を理解する", listOf(
            Lesson(3, "所有者とスコープ", listOf(
                q("値の所有者は同時にいくつ？", "1 つ", "2 つ", "無制限", "0 のみ", "Rust の各値には所有者が 1 つあります。"),
                code("fn main() {\n    let x = 4;\n    { let x = 9; }\n    println!(\"{}\", x);\n}", "4", "9", "13", "エラー", "内側の { } で作った x は外側の x とは別の変数です。外側の x は 4 のままです。"),
                tile("文字列の所有者を作ろう", "let s = String::new();", "let で変数 s を作り、= で String::new() が作った空の文字列を持たせ、; で文を終えます。", "s", "String::new();", "=", "let"),
                q("所有者がスコープを抜けると？", "値が破棄される", "必ずコピー", "永久に残る", "自動で返る", "値の持ち主が使える範囲を抜けると、drop という後片付けの処理が呼ばれます。"),
                q("ヒープ上の文字列を所有する型は？", "String", "&str", "char", "bool", "String は文字列のメモリを所有します。")
            )),
            Lesson(4, "ムーブ", listOf(
                q("String を別の変数に代入すると通常は？", "所有権が移る", "深いコピー", "参照になる", "整数になる", "String は代入で自動コピーされず、値の持ち主が移ります。これをムーブと呼びます。"),
                code("fn main() {\n    let a = String::from(\"Rust\");\n    let b = a;\n    println!(\"{}\", b);\n}", "Rust", "a", "b", "エラー", "b が新しい所有者になります。"),
                tile("s の所有権を t に移そう", "let t = s;", "let で変数 t を作り、= で String の持ち主を s から t に移し、; で文を終えます。", "t", "=", "s;", "let"),
                q("ムーブ後の元の String は？", "そのまま使えない", "空文字になる", "必ず使える", "数値になる", "持ち主が移った後に元の変数を使うと、実行前の検査でエラーになります。"),
                q("所有する String を関数に渡すと？", "通常ムーブする", "必ず借用", "必ず複製", "何も起きない", "String の値をそのまま関数に渡すと、その値の持ち主も関数へ移ります。")
            )),
            Lesson(5, "コピーと複製", listOf(
                q("String を明示的に複製するメソッドは？", "clone()", "move()", "borrow()", "copy_all()", "clone は独立した文字列を作れます。"),
                code("fn main() {\n    let a = 7;\n    let b = a;\n    println!(\"{}\", a + b);\n}", "14", "7", "0", "エラー", "整数は代入で値がコピーされる性質（Copy）があるので、a も使えます。7 + 7 で 14 です。"),
                tile("s を明示的に複製しよう", "let t = s.clone();", "let で変数 t を作り、= で s.clone() が複製した文字列を入れ、; で文を終えます。元の s も使えます。", "=", "t", "let", "s.clone();"),
                q("Copy を実装している型は？", "i32", "String", "Vec<i32>", "Box<i32>", "i32 は代入時にコピーされます。"),
                q("clone 後の元の String は？", "引き続き使える", "破棄される", "常に空になる", "ムーブ済み", "clone は元の値の持ち主を変えずに複製するので、元の String も使えます。")
            ))
        )),
        UnitContent("借用と参照", "所有権を渡さずに使う", listOf(
            Lesson(6, "共有参照", listOf(
                q("共有参照を作る記号は？", "&", "*", "!", "@", "&s で s を借用します。"),
                code("fn main() {\n    let s = String::from(\"abc\");\n    let r = &s;\n    println!(\"{}\", r.len());\n}", "3", "0", "1", "エラー", "len() は文字列の保存に使うバイト数を返します。半角英字の abc は 3 バイトです。"),
                tile("s を共有借用しよう", "let r = &s;", "let で変数 r を作り、= で s を読むための参照 &s を入れ、; で文を終えます。s の持ち主は変わりません。", "r", "let", "&s;", "="),
                q("共有参照から通常できないことは？", "値の変更", "値の読み取り", "関数への受け渡し", "複数の参照作成", "&T から通常は参照先を変更できません。"),
                q("借用すると所有権は？", "元の所有者に残る", "必ず移る", "消える", "複製される", "参照は値を所有しません。")
            )),
            Lesson(7, "可変参照", listOf(
                q("可変参照の型の形は？", "&mut T", "&const T", "mut &", "*T!", "&mut T は参照先を変更できます。"),
                code("fn main() {\n    let mut x = 1;\n    let r = &mut x;\n    *r += 2;\n    println!(\"{}\", x);\n}", "3", "1", "2", "エラー", "*r で参照先の値を変更します。"),
                tile("x を可変借用しよう", "let r = &mut x;", "let で変数 r を作り、= で x を変更できる参照 &mut x を入れ、; で文を終えます。", "&mut", "x;", "r", "let", "="),
                q("同時に使える同じ値への可変参照は？", "1 つ", "2 つ", "無制限", "常に 0", "同じ値を変更できる参照は、同時には 1 つだけ使えます。その間、同じ値を別の参照から読むこともできません。"),
                q("参照先を読み書きする演算子は？", "*", "&", "?", "::", "*r は参照 r が指す値を読み書きする書き方です。これを参照外しと呼びます。")
            )),
            Lesson(8, "スライスと寿命", listOf(
                q("文字列スライスの型は？", "&str", "String", "char", "str[]", "&str は文字列の一部を借用します。"),
                code("fn main() {\n    let s = \"Rust\";\n    println!(\"{}\", &s[0..2]);\n}", "Ru", "Rust", "st", "エラー", "0..2 は位置 0 から位置 2 の手前までの 2 バイトを選びます。Rust は半角英字なので文字の途中で切れず、Ru になります。"),
                tile("配列 a の全体を借用しよう", "let s = &a[..];", "let で変数 s を作り、= で配列 a の全範囲 [..] を借りる &a[..] を入れ、; で文を終えます。", "s", "&a[..];", "let", "="),
                q("参照は参照先より長く生きられる？", "生きられない", "常に生きられる", "String のみ可能", "整数のみ可能", "元の値がなくなった後に参照を使わないよう、実行前の検査で防ぎます。"),
                q("ライフタイム注釈の例は？", "'a", "@a", "#a", "~a", "'a は、参照が使える期間の関係に付ける名前です。")
            ))
        )),
        UnitContent("構造体と列挙型", "データの形と match", listOf(
            Lesson(9, "構造体", listOf(
                q("構造体を定義するのは？", "struct", "class", "object", "record", "struct で関連するデータをまとめます。"),
                code("struct Point { x: i32 }\nfn main() {\n    let p = Point { x: 8 };\n    println!(\"{}\", p.x);\n}", "8", "x", "Point", "エラー", "構造体の中の名前付きの値をフィールドと呼びます。p.x は p の中の x を読むので 8 です。"),
                tile("空の構造体を定義しよう", "struct Marker;", "struct で Marker という名前の構造体を定義し、データを持たない形なので ; で終えます。", "Marker;", "struct"),
                q("メソッドを定義するブロックは？", "impl", "use", "mod", "let", "impl の中に、その型に関係する関数や、値に対して呼ぶメソッドを書きます。"),
                q("自身を共有借用する引数は？", "&self", "self!", "this", "&class", "&self はメソッドを呼び出した値そのものを、変更せずに借りて使うための引数です。")
            )),
            Lesson(10, "列挙型と Option", listOf(
                q("列挙型を定義するのは？", "enum", "struct", "trait", "type!", "enum は複数の形から 1 つを選ぶ型を定義します。それぞれの形をバリアントと呼びます。"),
                code("fn main() {\n    let n = Some(3);\n    println!(\"{}\", n.is_some());\n}", "true", "false", "3", "エラー", "Some は値が存在することを表します。"),
                tile("値がない Option を宣言しよう", "let n: Option<i32> = None;", "let で変数 n を作り、: Option<i32> で整数が入る場合の型を指定し、= None で値がない状態にして、; で文を終えます。", "None;", "let", "n:", "Option<i32>", "="),
                q("Option のバリアントは？", "Some と None", "Ok と Err", "Yes と No", "True と False", "Option の取りうる形（バリアント）は、値がある Some と値がない None です。"),
                q("Result の成功を表すのは？", "Ok", "Some", "None", "Success", "Result は Ok と Err を持ちます。")
            )),
            Lesson(11, "match で分岐", listOf(
                q("match のアーム全体に必要なのは？", "全場合を網羅する", "1 つだけ必要", "整数のみ", "順序は常に無関係", "match では、どの値が来ても処理を選べるようにすべての場合を書く必要があります。足りないと実行前の検査でエラーになります。"),
                code("fn main() {\n    let n = 2;\n    let s = match n {\n        1 => \"一\",\n        _ => \"その他\",\n    };\n    println!(\"{}\", s);\n}", "その他", "一", "2", "エラー", "2 は 1 に一致せず、_ に一致します。"),
                tile("何にでも一致するアームを書こう", "_ => 0,", "_ はどの値にも一致し、=> の右側の 0 を結果にして、, で分岐の 1 行を区切ります。", "0,", "=>", "_"),
                q("単一パターンの簡潔な条件分岐は？", "if let", "for let", "when", "switch", "if let は、値が特定の形や条件（パターン）に合うときだけ処理したい場合に使えます。"),
                q("match のアームで使う矢印は？", "=>", "->", "<-", "==", "=> の左に値が一致する条件を書き、右にそのときの処理や結果を書きます。")
            ))
        ))
    )
    val lessons = units.flatMap { it.lessons }
}
