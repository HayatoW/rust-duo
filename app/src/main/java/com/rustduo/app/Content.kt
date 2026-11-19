package com.rustduo.app

private fun q(prompt: String, answer: String, a: String, b: String, c: String, why: String) =
    Question(prompt, listOf(answer, a, b, c), answer, why)
private fun code(source: String, answer: String, a: String, b: String, c: String, why: String) =
    Question("このコードの出力は？", listOf(answer, a, b, c), answer, why, Kind.CODE, source)
private fun tile(prompt: String, answer: String, vararg words: String) =
    Question(prompt, words.toList(), answer, "正しい構文は「$answer」です。", Kind.TILES)

object Curriculum {
    val units = listOf(
        UnitContent("基本", "変数・可変性・型", listOf(
            Lesson(0, "変数との出会い", listOf(
                q("変数を宣言するキーワードは？", "let", "var", "val", "define", "Rust では let で変数を束縛します。"),
                code("fn main() {\n    let x = 5;\n    println!(\"{}\", x);\n}", "5", "x", "0", "エラー", "x に束縛された 5 が表示されます。"),
                tile("可変な変数 x に 5 を代入しよう", "let mut x = 5;", "let", "mut", "x", "=", "5;"),
                q("let x = 5; の x は？", "変更不可", "常に可変", "文字列", "関数", "mut がない変数はデフォルトで不変です。"),
                q("文の終わりに使う記号は？", ";", ":", ",", "#", "多くの文はセミコロンで終わります。")
            )),
            Lesson(1, "可変性", listOf(
                q("変数を変更可能にするのは？", "mut", "const", "static", "use", "let mut で可変な束縛を作ります。"),
                code("fn main() {\n    let mut x = 2;\n    x += 3;\n    println!(\"{}\", x);\n}", "5", "2", "3", "エラー", "可変な x に 3 を足すので 5 です。"),
                tile("不変な変数 n に 8 を代入しよう", "let n = 8;", "8;", "=", "n", "let"),
                q("同名の let で再束縛する機能は？", "シャドーイング", "借用", "移動", "継承", "let を使って同じ名前を再利用できます。"),
                q("定数を宣言するのは？", "const", "mut", "impl", "mod", "const には型注釈と定数式が必要です。")
            )),
            Lesson(2, "型の基礎", listOf(
                q("真偽値の型は？", "bool", "boolean", "bit", "truth", "bool は true と false を表します。"),
                code("fn main() {\n    let n: i32 = 3;\n    println!(\"{}\", n * 2);\n}", "6", "3", "32", "エラー", "i32 は符号付き 32 ビット整数です。"),
                tile("真偽値を宣言しよう", "let ok = true;", "true;", "let", "=", "ok"),
                q("1 文字を表す型は？", "char", "str", "String", "byte", "char は Unicode スカラー値を表します。"),
                q("符号なし 32 ビット整数は？", "u32", "i32", "f32", "bool", "u は符号なし整数を表します。")
            ))
        )),
        UnitContent("所有権", "値の持ち主を理解する", listOf(
            Lesson(3, "所有者とスコープ", listOf(
                q("値の所有者は同時にいくつ？", "1 つ", "2 つ", "無制限", "0 のみ", "Rust の各値には所有者が 1 つあります。"),
                code("fn main() {\n    let x = 4;\n    { let x = 9; }\n    println!(\"{}\", x);\n}", "4", "9", "13", "エラー", "内側の x は別のスコープの束縛です。"),
                tile("文字列の所有者を作ろう", "let s = String::new();", "s", "String::new();", "=", "let"),
                q("所有者がスコープを抜けると？", "値が破棄される", "必ずコピー", "永久に残る", "自動で返る", "所有者の終了時に drop が呼ばれます。"),
                q("ヒープ上の文字列を所有する型は？", "String", "&str", "char", "bool", "String は文字列のメモリを所有します。")
            )),
            Lesson(4, "ムーブ", listOf(
                q("String を別の変数に代入すると通常は？", "所有権が移る", "深いコピー", "参照になる", "整数になる", "Copy でない String はムーブされます。"),
                code("fn main() {\n    let a = String::from(\"Rust\");\n    let b = a;\n    println!(\"{}\", b);\n}", "Rust", "a", "b", "エラー", "b が新しい所有者になります。"),
                tile("s の所有権を t に移そう", "let t = s;", "t", "=", "s;", "let"),
                q("ムーブ後の元の String は？", "そのまま使えない", "空文字になる", "必ず使える", "数値になる", "ムーブ後の束縛の使用はコンパイルエラーです。"),
                q("所有する String を関数に渡すと？", "通常ムーブする", "必ず借用", "必ず複製", "何も起きない", "値渡しでは所有権を渡します。")
            )),
            Lesson(5, "コピーと複製", listOf(
                q("String を明示的に複製するメソッドは？", "clone()", "move()", "borrow()", "copy_all()", "clone は独立した文字列を作れます。"),
                code("fn main() {\n    let a = 7;\n    let b = a;\n    println!(\"{}\", a + b);\n}", "14", "7", "0", "エラー", "整数は Copy なので a も利用できます。"),
                tile("s を明示的に複製しよう", "let t = s.clone();", "=", "t", "let", "s.clone();"),
                q("Copy を実装している型は？", "i32", "String", "Vec<i32>", "Box<i32>", "i32 は代入時にコピーされます。"),
                q("clone 後の元の String は？", "引き続き使える", "破棄される", "常に空になる", "ムーブ済み", "clone は元の所有権を奪いません。")
            ))
        )),
        UnitContent("借用と参照", "所有権を渡さずに使う", listOf(
            Lesson(6, "共有参照", listOf(
                q("共有参照を作る記号は？", "&", "*", "!", "@", "&s で s を借用します。"),
                code("fn main() {\n    let s = String::from(\"abc\");\n    let r = &s;\n    println!(\"{}\", r.len());\n}", "3", "0", "1", "エラー", "ASCII の abc は 3 バイトです。"),
                tile("s を共有借用しよう", "let r = &s;", "r", "let", "&s;", "="),
                q("共有参照から通常できないことは？", "値の変更", "値の読み取り", "関数への受け渡し", "複数の参照作成", "&T から通常は参照先を変更できません。"),
                q("借用すると所有権は？", "元の所有者に残る", "必ず移る", "消える", "複製される", "参照は値を所有しません。")
            )),
            Lesson(7, "可変参照", listOf(
                q("可変参照の型の形は？", "&mut T", "&const T", "mut &", "*T!", "&mut T は参照先を変更できます。"),
                code("fn main() {\n    let mut x = 1;\n    let r = &mut x;\n    *r += 2;\n    println!(\"{}\", x);\n}", "3", "1", "2", "エラー", "*r で参照先の値を変更します。"),
                tile("x を可変借用しよう", "let r = &mut x;", "&mut", "x;", "r", "let", "="),
                q("同時に使える同じ値への可変参照は？", "1 つ", "2 つ", "無制限", "常に 0", "可変参照は排他的なアクセスを保証します。"),
                q("参照先を読み書きする演算子は？", "*", "&", "?", "::", "* は参照外し演算子です。")
            )),
            Lesson(8, "スライスと寿命", listOf(
                q("文字列スライスの型は？", "&str", "String", "char", "str[]", "&str は文字列の一部を借用します。"),
                code("fn main() {\n    let s = \"Rust\";\n    println!(\"{}\", &s[0..2]);\n}", "Ru", "Rust", "st", "エラー", "0..2 は先頭 2 バイトです。ASCII なので境界も有効です。"),
                tile("配列 a の全体を借用しよう", "let s = &a[..];", "s", "&a[..];", "let", "="),
                q("参照は参照先より長く生きられる？", "生きられない", "常に生きられる", "String のみ可能", "整数のみ可能", "ダングリング参照をコンパイラが防ぎます。"),
                q("ライフタイム注釈の例は？", "'a", "@a", "#a", "~a", "'a は参照の寿命の関係を表す名前です。")
            ))
        )),
        UnitContent("構造体と列挙型", "データの形と match", listOf(
            Lesson(9, "構造体", listOf(
                q("構造体を定義するのは？", "struct", "class", "object", "record", "struct で関連するデータをまとめます。"),
                code("struct Point { x: i32 }\nfn main() {\n    let p = Point { x: 8 };\n    println!(\"{}\", p.x);\n}", "8", "x", "Point", "エラー", "ドットでフィールドにアクセスします。"),
                tile("空の構造体を定義しよう", "struct Marker;", "Marker;", "struct"),
                q("メソッドを定義するブロックは？", "impl", "use", "mod", "let", "impl に関連関数やメソッドを書きます。"),
                q("自身を共有借用する引数は？", "&self", "self!", "this", "&class", "&self はインスタンスを共有借用します。")
            )),
            Lesson(10, "列挙型と Option", listOf(
                q("列挙型を定義するのは？", "enum", "struct", "trait", "type!", "enum で複数のバリアントを表現します。"),
                code("fn main() {\n    let n = Some(3);\n    println!(\"{}\", n.is_some());\n}", "true", "false", "3", "エラー", "Some は値が存在することを表します。"),
                tile("値がない Option を宣言しよう", "let n: Option<i32> = None;", "None;", "let", "n:", "Option<i32>", "="),
                q("Option のバリアントは？", "Some と None", "Ok と Err", "Yes と No", "True と False", "Option は値の有無を表します。"),
                q("Result の成功を表すのは？", "Ok", "Some", "None", "Success", "Result は Ok と Err を持ちます。")
            )),
            Lesson(11, "match で分岐", listOf(
                q("match のアーム全体に必要なのは？", "全場合を網羅する", "1 つだけ必要", "整数のみ", "順序は常に無関係", "match はすべての値を網羅しないとコンパイルエラーになります。"),
                code("fn main() {\n    let n = 2;\n    let s = match n {\n        1 => \"一\",\n        _ => \"その他\",\n    };\n    println!(\"{}\", s);\n}", "その他", "一", "2", "エラー", "2 は 1 に一致せず、_ に一致します。"),
                tile("何にでも一致するアームを書こう", "_ => 0,", "0,", "=>", "_"),
                q("単一パターンの簡潔な条件分岐は？", "if let", "for let", "when", "switch", "if let は一部のパターンだけを扱うときに便利です。"),
                q("match のアームで使う矢印は？", "=>", "->", "<-", "==", "パターン => 式 の形で記述します。")
            ))
        ))
    )
    val lessons = units.flatMap { it.lessons }
}
