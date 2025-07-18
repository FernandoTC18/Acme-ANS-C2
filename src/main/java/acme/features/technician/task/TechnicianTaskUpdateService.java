
package acme.features.technician.task;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.task.Task;
import acme.entities.task.TaskType;
import acme.realms.Technician;

@GuiService
public class TechnicianTaskUpdateService extends AbstractGuiService<Technician, Task> {

	@Autowired
	private TechnicianTaskRepository repository;


	@Override
	public void authorise() {
		boolean status;
		int id;
		Task task;
		Technician technician;

		if (super.getRequest().hasData("id")) {

			id = super.getRequest().getData("id", int.class);
			task = this.repository.findTaskById(id);

			// Para comprobar que no se introduzca un tipo de tarea que no exista
			if (super.getRequest().hasData("type")) {
				@SuppressWarnings("unused")
				TaskType type = super.getRequest().getData("type", TaskType.class);
			}

			technician = task == null ? null : task.getTechnician();

			status = task != null && task.isDraftMode() && super.getRequest().getPrincipal().hasRealm(technician);

		} else
			status = false;
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Task task;
		int id;

		id = super.getRequest().getData("id", int.class);
		task = this.repository.findTaskById(id);

		super.getBuffer().addData(task);
	}

	@Override
	public void bind(final Task task) {
		super.bindObject(task, "type", "description", "priority", "estimatedDuration");

	}

	@Override
	public void validate(final Task task) {
		boolean status;

		status = task.isDraftMode();

		super.state(status, "*", "acme.validation.updatePublishedTask.message");

	}

	@Override
	public void perform(final Task task) {
		this.repository.save(task);
	}

	@Override
	public void unbind(final Task task) {
		Dataset dataset;
		SelectChoices taskTypes;

		taskTypes = SelectChoices.from(TaskType.class, task.getType());

		dataset = super.unbindObject(task, "type", "description", "priority", "estimatedDuration");
		//dataset.put("confirmation", false);
		//dataset.put("readonly", false);
		dataset.put("type", taskTypes);

		super.getResponse().addData(dataset);
	}

}
