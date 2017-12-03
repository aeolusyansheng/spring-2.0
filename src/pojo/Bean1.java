package pojo;

public class Bean1 {

	private String name;
	private int age;
	private Bean2 bean;

	public void setBean(Bean2 bean) {
		this.bean = bean;
	}

	public Bean2 getBean() {
		return this.bean;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

}
