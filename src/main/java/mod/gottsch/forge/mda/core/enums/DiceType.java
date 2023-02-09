package mod.gottsch.forge.mda.core.enums;

/**
 * 
 * @author Mark Gottschling Feb 8, 2023
 *
 */
public enum DiceType implements IDiceType {
	D4(4, 2.5F),
	D6(6, 3.5F),
	D8(8, 4.5F),
	D10(10, 5.5F),
	D12(12, 6.5F),
	D20(20, 10.5F);

	private static DiceType defaultDice = D6;
	private Integer dice;
	private Float avg;
	
	DiceType(Integer dice, Float avg) {
		this.dice = dice;
		this.avg = avg;
	}
	
	public Integer getDice() {
		return dice;
	}
	
	public Float getAvg() {
		return avg;
	}
	
	public static Integer validate(Integer d) {
		try {
			return valueOf(d).getDice();
		} catch(Exception e) {
			return defaultDice.getDice();
		}
	}
	
	public static DiceType valueOf(Integer d) {
		return valueOf("D" + d.toString());
	}
}
