package com.qingcheng.pojo.goods;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
/**
 * album实体类
 * @author Administrator
 *
 */
@Table(name="tb_album")
public class Album implements Serializable{

	@Id
	private Long id;//编号

	private Integer imageNum;//图片数量

	private String	seq;//排序
	

	private String title;//相册名称

	private String image;//相册封面

	private String imageItems;//图片列表

	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}

	public String getImageItems() {
		return imageItems;
	}
	public void setImageItems(String imageItems) {
		this.imageItems = imageItems;
	}

	public Integer getImageNum() {
		return imageNum;
	}

	public void setImageNum(Integer imageNum) {
		this.imageNum = imageNum;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}
}
