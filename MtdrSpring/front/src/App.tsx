import { useEffect, useState } from "react";
import { Button, CircularProgress, Select, MenuItem, InputLabel, FormControl } from "@mui/material";
import "./App.css";
import { getTasks, updateTask, deleteTask } from "./api/task";
import { getUsers } from "./api/user";
import { Task } from "./models/Task";
import { User } from "./models/User";
import ErrorMessage from "./components/Error/Error";
import TaskTable from "./components/TaskTable";
import MainTitle from "./components/MainTitle";
import AddModal from "./components/AddModal/AddModal";
import KpiModal from "./components/KpiModal/KpiModal";

function App() {
  const [loading, setLoading] = useState<boolean>(false);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState<string>("");
  const [showAddModal, setShowAddModal] = useState<boolean>(false);
  const [selectedSprint, setSelectedSprint] = useState<number | "all">("all");
  const [sprints, setSprints] = useState<number[]>([1, 2, 3, 4, 5]); // Example sprint IDs, replace with your actual sprints
  const [showStats, setShowStats] = useState<boolean>(false);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [tasksData, usersData] = await Promise.all([
          getTasks(),
          getUsers(),
        ]);
        setTasks(
          tasksData.sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
        );
        setUsers(usersData);
        console.log(`Users: ${usersData}`);
        console.log(`Users: ${usersData}`);
      } catch (error) {
        console.error(error);
        setError("Error fetching data");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
    setSprints([1, 2, 3, 4, 5]);
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

  const handleStateChange = async (task: Task, newState: string) => {
    try {
      const updatedTask = { ...task, state: newState };
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

  const handleEdit = async (task: Task) => {
    console.log("Edit task:", task);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTask(id);
      // Update the local state instead of reloading
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

  // Filter tasks based on selected sprint
  const filteredTasks = selectedSprint === "all" ? tasks : tasks.filter((task) => task.id_Sprint === selectedSprint);
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
                sprintId={1}
                addTask={handleAddTask}
              />
              {/** Constant for now. */}
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
              {/* Sprint Filter */}
              <FormControl sx={{ width: '30%', backgroundColor: 'primary.main', color: 'white', margin: '10px' }}>
                <InputLabel id="sprint-select-label"></InputLabel>
                <Select
                  sx={{color: 'white' }}
                  labelId="sprint-select-label"
                  value={selectedSprint}
                  label="Sprint"
                  onChange={(e) => setSelectedSprint(e.target.value as number | "all")} // Explicitly cast value to `number | "all"`
                >
                  <MenuItem value="all">All Sprints</MenuItem>
                  {sprints.map((sprint) => (
                    <MenuItem  key={sprint} value={sprint}>
                      Sprint {sprint}
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

        <KpiModal
          tasks={tasks}
          open={showStats}
          onClose={handleCloseStats}
          users={users}
        />
      </div>
    </div>
  );
}

export default App;
