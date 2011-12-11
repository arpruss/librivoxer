$max = 0;
while(<>) {
    s/[\r\n]//;
    @g = split(", ", $_);
    $max = @g if $max < @g;
    foreach (@g) {
        s/ $//;
        print "\"$_\",\n";
    }
}
#print "$max\n";
