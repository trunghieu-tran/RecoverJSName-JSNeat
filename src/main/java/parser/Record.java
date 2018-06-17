package parser;

import java.util.Objects;

public class Record {
	
	String pe, name, relationship;
	int type;
	
	public Record(String pe, String name, String relationship) {
		this.pe = pe;
		this.name = name;
		this.relationship = relationship;
	}
	
	//Type = 1: variables and variables
	//type = 0: variables and program entities 
	public Record(String pe, String name, String relationship, int type) {
		this.pe = pe;
		this.name = name;
		this.relationship = relationship;
		this.type = type;
	}
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Record other = (Record) obj;
        
        if (pe == null) {
            if (other.pe != null)
                return false;
        } else if (!pe.equals(other.pe))
            return false;
        
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        
        if (relationship == null) {
            if (other.relationship != null)
                return false;
        } else if (!relationship.equals(other.relationship))
            return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pe, name, relationship);
    }
	
}
