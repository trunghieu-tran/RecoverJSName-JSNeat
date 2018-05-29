var sum = {
	first: 1,
	second : 2,
	total : function(f, s) {
		return f + s;
	}
};
var a;
a = sum.first;
var b = sum.second;
b = a;
var c = sum.total(a, b);
c = a + b;