function test () 
{
	var person = {
	firstName: "John",
	lastName : "Doe",
	id       : 5566,
	wholeName : function(fn, ln) {
		return fn + " " + ln;
	}
	};
	var x;
	x = person.firstName;
	var y = person.lastName;
	y = x;
	var z = person.wholeName(x, y);
	z = x + y;
}

function test2 () 
{
	var person = {
	firstName: "John",
	lastName : "Doe",
	id       : 5566,
	wholeName : function(fn, ln) {
		return fn + " " + ln;
	}
	};
	var x;
	x = person.firstName;
	var y = person.lastName;
	y = x;
	var z = person.wholeName(x, y);
	z = x + y;
}