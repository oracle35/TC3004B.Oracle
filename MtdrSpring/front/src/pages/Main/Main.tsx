import { useEffect, useState } from "react";
import {
  Button,
  CircularProgress,
  Select,
  MenuItem,
  InputLabel,
  FormControl,
} from "@mui/material";
import { getTasks, updateTask, deleteTask } from "../../api/task";
import { getUsers } from "../../api/user";
import { getSprints } from "../../api/sprint"; // <-- Added getSprints import
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import ErrorMessage from "../../components/ErrorMessage/ErrorMessage";
import TaskTable from "../../components/TaskTable";
import AddModal from "../../components/AddModal/AddModal";
import { Sprint } from "../../models/Sprint"; // using Sprint model
import { getCurrentSprint } from "../../utils/sprint";
import SprintWarning from "../../components/SprintWarning";
import BacklogDrawer from "../../components/Backlog/Backlog";
import NavBar from "../../components/NavBar/NavBar.tsx";
import MainTitle from "../../components/MainTitle.tsx";
import { Subtitle } from "../../components/Subtitle.tsx";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import EditModal from "../../components/EditModal/EditModal.tsx";

// import styles from "./Main.module.css";

function MainPage() {
  const [loading, setLoading] = useState<boolean>(false);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState<string>("");
  const [showAddModal, setShowAddModal] = useState<boolean>(false);
  const [selectedSprint, setSelectedSprint] = useState<number | "all">("all");
  const [currentSprint, setCurrentSprint] = useState<Sprint>();
  const [openBacklog, setOpenBacklog] = useState<boolean>(false);
  const [selectedSprintObject, setSelectedSprintObject] = useState<
    Sprint | undefined
  >(undefined);

  // This selected task is used for the editing modal.
  const [selectedTask, setSelectedTask] = useState<Task>();
  const [showEditingModal, setShowEditingModal] = useState<boolean>(false);

  const toggleBacklog = (newOpen: boolean) => {
    setOpenBacklog(newOpen);
  };

  // TODO: Refactor this into having dynamic sprints depending on the user and its project.
  const [sprints, setSprints] = useState<Sprint[]>([]);

  // Fetch basic data
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [tasksData, usersData, sprintsData, currentSprint] =
          await Promise.all([
            getTasks(),
            getUsers(),
            getSprints(),
            getCurrentSprint(),
          ]);
        setTasks(
          tasksData.sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
        );
        setUsers(usersData);
        setSprints(sprintsData.sort((a, b) => a.name.localeCompare(b.name)));
        setCurrentSprint(currentSprint);
        // set sprint objects from API
      } catch (error) {
        console.error(error);
        setError("Error fetching data");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
    if (selectedSprint === "all") {
      setSelectedSprintObject(undefined);
    } else {
      const sprint = sprints.find((s) => s.id_Sprint === selectedSprint);
      setSelectedSprintObject(sprint);
    }
  }, [selectedSprint, sprints]);

  const reloadTasks = async () => {
    if (!loading) {
      setLoading(true);
      try {
        const tasksData = await getTasks();
        setTasks(
          tasksData.sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
        );
      } catch (error) {
        console.error(error);
        setError("Error reloading tasks");
      } finally {
        setLoading(false);
      }
    }
  };

  const handleStateChange = async (
    task: Task,
    newState: string,
    realHours: number
  ) => {
    try {
      const updatedTask = { ...task, state: newState, hoursReal: realHours };
      await updateTask(task.id_Task, updatedTask);
      setTasks((prevTasks) =>
        prevTasks
          .map((t) => (t.id_Task === task.id_Task ? updatedTask : t))
          .sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
      );
    } catch (error) {
      console.error(error);
      setError("Error updating task state");
    }
  };

  const handleOpenEditingModal = async (task: Task) => {
    setSelectedTask(task);
    setShowEditingModal(true);
  };

  const handleEdit = async (updatedTaskFromModal: Task) => {
    setTasks((prevTasks) =>
      prevTasks
        .map((t) =>
          t.id_Task === updatedTaskFromModal.id_Task ? updatedTaskFromModal : t
        )
        .sort((a: Task, b: Task) => a.description.localeCompare(b.description))
    );
  };

  const handleCloseEditingModal = () => {
    setShowEditingModal(false);
    setSelectedTask(undefined);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTask(id);
      setTasks((prevTasks) =>
        prevTasks
          .filter((t) => t.id_Task !== id)
          .sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
      );
    } catch (error) {
      console.error(error);
      setError("Error deleting task");
    }
  };

  const handleOpenAddModal = () => {
    setShowAddModal(true);
  };

  const handleCloseAddModal = () => {
    setShowAddModal(false);
  };

  const handleAddTask = async () => {
    try {
      const tasksData = await getTasks();
      setTasks(
        tasksData.sort((a: Task, b: Task) =>
          a.description.localeCompare(b.description)
        )
      );
    } catch (error) {
      console.error(error);
      setError("Error adding task");
    }
  };

  // Filter tasks based on selected sprint (using id_Sprint)
  const filteredTasks =
    selectedSprint === "all"
      ? tasks
      : tasks.filter((task) => task.id_Sprint === selectedSprint);
  // If the page is loading, nothing else.
  if (loading) {
    return (
      <div>
        <MainTitle>Oracle Task Management System</MainTitle>
        <CircularProgress />
      </div>
    );
  }

  return (
    <div className="flex flex-col fade-in-up">
      <div>
        <MainTitle>Oracle Task Management System</MainTitle>

        {/* Mueve el NavBar aquí debajo del Logout */}
        <NavBar />

        {currentSprint ? (
          <Subtitle>
            <AccessTimeIcon
              sx={{
                verticalAlign: "middle",
                mr: 0.5,
                fontSize: "1.1rem",
                color: "white",
              }}
            />{" "}
            Current Sprint: {currentSprint.name}
          </Subtitle>
        ) : (
          <div />
        )}

        {error && <ErrorMessage error={error} />}

        <BacklogDrawer
          open={openBacklog}
          onClose={toggleBacklog}
          tasks={tasks}
          sprints={sprints}
        />
        <div>
          <div>
            <AddModal
              open={showAddModal}
              onClose={handleCloseAddModal}
              reloadTable={reloadTasks}
              setLoading={setLoading}
              // Use the selected sprint if not "all", otherwise default to the first sprint if available
              sprintId={
                selectedSprint === "all"
                  ? sprints[0]?.id_Sprint || 0
                  : selectedSprint
              }
              addTask={handleAddTask}
            />

            <Button
              onClick={handleOpenAddModal}
              variant="outlined"
              style={{
                margin: "10px",
                padding: "10px",
                color: "white", // Cambia el color del texto a blanco
                borderColor: "#c74634", // Color del borde
              }}
              sx={{
                "&:hover": {
                  borderColor: "#9e2a2a", // Color del borde al pasar el ratón
                  backgroundColor: "#9e2a2a", // Fondo más oscuro en hover
                },
              }}
            >
              Add Task
            </Button>

            <Button
              onClick={() => toggleBacklog(true)}
              variant="outlined"
              style={{
                margin: "10px",
                padding: "10px",
                color: "white", // Cambia el color del texto a blanco
                borderColor: "#c74634", // Color del borde
              }}
              sx={{
                "&:hover": {
                  borderColor: "#9e2a2a", // Color del borde al pasar el ratón
                  backgroundColor: "#9e2a2a", // Fondo más oscuro en hover
                },
              }}
            >
              Backlog
            </Button>

            <h3>Filter by Sprint</h3>
            <FormControl
              sx={{
                width: "30%",
                backgroundColor: "primary.main",
                color: "white",
                margin: "10px",
              }}
            >
              <InputLabel id="sprint-select-label"></InputLabel>
              <Select
                sx={{ color: "white", backgroundColor: "#c74634" }}
                labelId="sprint-select-label"
                value={selectedSprint}
                id="blehhhh"
                label="Sprint"
                onChange={(e) =>
                  setSelectedSprint(e.target.value as number | "all")
                }
              >
                <MenuItem value="all">All Sprints</MenuItem>
                {sprints.map((sprint) => (
                  <MenuItem key={sprint.id_Sprint} value={sprint.id_Sprint}>
                    {sprint.name}{" "}
                    {currentSprint?.name == sprint.name
                      ? "(Current Sprint)"
                      : ""}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <SprintWarning selectedSprint={selectedSprintObject} />
            <TaskTable
              tasks={filteredTasks}
              users={users}
              handleDelete={handleDelete}
              handleEdit={handleOpenEditingModal}
              handleStateChange={handleStateChange}
            />
            <EditModal
              open={showEditingModal}
              onClose={handleCloseEditingModal}
              taskToEdit={selectedTask}
              onTaskUpdated={handleEdit}
            />
          </div>
        </div>
      </div>
    </div>
  );
}

export default MainPage;
