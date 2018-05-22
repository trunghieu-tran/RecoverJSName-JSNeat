var person = {
	firstName: "John",
	lastName : "Doe",
	id       : 5566,
	fullName : function(fn, ln) {
		return fn + " " + ln;
	}
};
var x;
x = person.firstName;
var y = person.lastName;
person.fullName(x, y);