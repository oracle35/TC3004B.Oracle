import { useEffect, useState } from "react";
import { Button, CircularProgress, Select, MenuItem, InputLabel, FormControl } from "@mui/material";
import "./App.css";
import { getTasks, updateTask, deleteTask } from "./api/task";
import { getUsers } from "./api/user";
import { getSprints } from "./api/sprint"; // <-- Added getSprints import
import { Task } from "./models/Task";
import { User } from "./models/User";
import ErrorMessage from "./components/Error/Error";
import TaskTable from "./components/TaskTable";
import MainTitle from "./components/MainTitle";
import AddModal from "./components/AddModal/AddModal";
import KpiModal from "./components/KpiModal/KpiModal";
import { Sprint } from "./models/Sprint"; // using Sprint model

function App() {
  const [loading, setLoading] = useState<boolean>(false);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState<string>("");
  const [showAddModal, setShowAddModal] = useState<boolean>(false);
  const [selectedSprint, setSelectedSprint] = useState<number | "all">("all");

  // Now sprints is an array of Sprint objects
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [showStats, setShowStats] = useState<boolean>(false);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [tasksData, usersData, sprintsData] = await Promise.all([
          getTasks(),
          getUsers(),
          getSprints(), // Fetch sprint objects from API
        ]);
        setTasks(
          tasksData.sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
        );
        setUsers(usersData);
        setSprints(sprintsData.sort((a, b) => a.name.localeCompare(b.name)));
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

  const handleStateChange = async (task: Task, newState: string, hrsReales: number) => {
    try {
      const updatedTask = { ...task, state: newState, hoursReal: hrsReales };
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
  }

  const handleEdit = async (task: Task) => {
    console.log("Edit task:", task);
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

  const handleOpen = () => {
    setShowAddModal(true);
  };

  const handleClose = () => {
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

  const handleShowStats = () => {
    setShowStats(true);
  };

  const handleCloseStats = () => {
    setShowStats(false);
  };

  // Filter tasks based on selected sprint (using id_Sprint)
  const filteredTasks =
    selectedSprint === "all" ? tasks : tasks.filter((task) => task.id_Sprint === selectedSprint);

  return (
    <div className="flex flex-col">
      <div>
        <MainTitle title="Oracle Task Management System" />

        {error && <ErrorMessage error={error} />}
        {loading && <CircularProgress />}

        {!loading && (
          <div>
            <div>
              <AddModal
                open={showAddModal}
                onClose={handleClose}
                reloadTable={reloadTasks}
                setLoading={setLoading}
                // Use the selected sprint if not "all", otherwise default to the first sprint if available
                sprintId={
                  selectedSprint === "all" ? (sprints[0]?.id_Sprint || 0) : selectedSprint
                }
                addTask={handleAddTask}
              />
              <Button
                onClick={handleShowStats}
                variant="outlined"
                style={{ margin: "10px", padding: "10px" }}
              >
                Show Stats
              </Button>
              <Button
                onClick={handleOpen}
                variant="outlined"
                style={{ margin: "10px", padding: "10px" }}
              >
                Add Task
              </Button>

              <h3>Filter by Sprint</h3>
              <FormControl
                sx={{ width: "30%", backgroundColor: "primary.main", color: "white", margin: "10px" }}
              >
                <InputLabel id="sprint-select-label"></InputLabel>
                <Select
                  sx={{ color: "white" }}
                  labelId="sprint-select-label"
                  value={selectedSprint}
                  label="Sprint"
                  onChange={(e) => setSelectedSprint(e.target.value as number | "all")}
                >
                  <MenuItem value="all">All Sprints</MenuItem>
                  {sprints.map((sprint) => (
                    <MenuItem key={sprint.id_Sprint} value={sprint.id_Sprint}>
                      {sprint.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <h3>Tasks</h3>
              <TaskTable
                tasks={filteredTasks}
                users={users}
                handleDelete={handleDelete}
                handleEdit={handleEdit}
                handleStateChange={handleStateChange}
              />
            </div>
          </div>
        )}

        <KpiModal tasks={tasks} open={showStats} onClose={handleCloseStats} users={users} sprints={sprints} />
      </div>
    </div>
  );
}

export default App;