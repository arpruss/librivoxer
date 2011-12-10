$n = 1;
open O, ">:encoding(UTF-8)", "assets/catalog$n.xml";
$count = 0;
while(<>) {
    if (/^<\/results>/) {
        last;
    }
    print O $_;
    if (/<\/book>/) {
        $count++;
        if ($count >= 640) {
            print O "</results>\n";
            close O;
            $n++;
            open O, ">:encoding(UTF-8)", "assets/catalog$n.xml";
            print O "<results>\n";
            $count = 0;
        }
    }
}
print O "</results>\n";