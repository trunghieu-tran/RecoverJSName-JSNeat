var person = {
	firstName: "John",
	lastName : "Doe",
	id       : 5566,
	fullName : function() {
		return this.firstName + " " + this.lastName;
	}
};
person.firstName;
person.fullName();
