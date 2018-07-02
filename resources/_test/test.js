function test (a,b,c) 
{
	var person = {
		firstName: "John",
		lastName : "Doe",
		id       : 5566,
		wholeName : function(fn, ln) {
			fn = ln.m();
			return fn + " " + ln;
		}
	};
	var x;
	x = person.firstName; 
	var y = person.lastName;
	var z = person.wholeName(x, y);
	z = person.firstName + y + a;
	person.lastName = z;
}
